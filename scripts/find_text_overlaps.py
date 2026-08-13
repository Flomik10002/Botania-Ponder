#!/usr/bin/env python3
"""
Heuristic scanner for Botania Ponder scenes: simulates each scene's timeline by
walking `scene.idle(N)` calls and flags cases where two showText / showOutlineWithText
windows would be visible on screen at the same time (overlapping [start, start+duration)
intervals) -- these render as text stacked on text.

Not a real Java parser. Good enough for this codebase's style: sequential
scene.xxx() statement calls, occasional for-loops with literal bounds, and
private static helper methods that also touch the timeline (idle/showText),
which get inlined.
"""
import re
import sys
from pathlib import Path
from dataclasses import dataclass, field

REPO_ROOT = Path(__file__).resolve().parent.parent
SCENES_DIR = REPO_ROOT / "src/main/java/dev/flomik/botania_ponder/ponder/scenes"

STRING_RE = re.compile(r'"((?:[^"\\]|\\.)*)"')


def strip_comments(src: str) -> str:
    out = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if c == '/' and i + 1 < n and src[i + 1] == '/':
            j = src.find('\n', i)
            i = n if j == -1 else j
            continue
        if c == '/' and i + 1 < n and src[i + 1] == '*':
            j = src.find('*/', i + 2)
            i = n if j == -1 else j + 2
            continue
        if c == '"':
            j = i + 1
            while j < n and src[j] != '"':
                if src[j] == '\\':
                    j += 1
                j += 1
            out.append(src[i:j + 1])
            i = j + 1
            continue
        if c == "'":
            j = i + 1
            while j < n and src[j] != "'":
                if src[j] == '\\':
                    j += 1
                j += 1
            out.append(src[i:j + 1])
            i = j + 1
            continue
        out.append(c)
        i += 1
    return ''.join(out)


def find_matching(src: str, open_idx: int) -> int:
    """Given index of an opening bracket, return index of its match."""
    pairs = {'(': ')', '{': '}', '[': ']'}
    opener = src[open_idx]
    closer = pairs[opener]
    depth = 0
    i = open_idx
    n = len(src)
    while i < n:
        c = src[i]
        if c == '"':
            i += 1
            while i < n and src[i] != '"':
                if src[i] == '\\':
                    i += 1
                i += 1
        elif c == "'":
            i += 1
            while i < n and src[i] != "'":
                if src[i] == '\\':
                    i += 1
                i += 1
        elif c == opener:
            depth += 1
        elif c == closer:
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def split_top_level_statements(body: str):
    """Split a method/block body into top-level statements. A statement ends at a
    top-level ';' or right after a top-level '{...}' block closes (for/if/etc)."""
    stmts = []
    buf = []
    depth = 0
    i, n = 0, len(body)
    while i < n:
        c = body[i]
        if c == '"':
            j = i + 1
            while j < n and body[j] != '"':
                if body[j] == '\\':
                    j += 1
                j += 1
            buf.append(body[i:j + 1])
            i = j + 1
            continue
        if c == "'":
            j = i + 1
            while j < n and body[j] != "'":
                if body[j] == '\\':
                    j += 1
                j += 1
            buf.append(body[i:j + 1])
            i = j + 1
            continue
        if c in '({[':
            depth += 1
            buf.append(c)
        elif c in ')}]':
            depth -= 1
            buf.append(c)
            if depth == 0 and c == '}':
                stmt = ''.join(buf).strip()
                if stmt:
                    stmts.append(stmt)
                buf = []
        elif c == ';' and depth == 0:
            stmt = ''.join(buf).strip()
            if stmt:
                stmts.append(stmt)
            buf = []
        else:
            buf.append(c)
        i += 1
    tail = ''.join(buf).strip()
    if tail:
        stmts.append(tail)
    return stmts


def split_top_level_commas(s: str):
    parts = []
    buf = []
    depth = 0
    i, n = 0, len(s)
    while i < n:
        c = s[i]
        if c == '"':
            j = i + 1
            while j < n and s[j] != '"':
                if s[j] == '\\':
                    j += 1
                j += 1
            buf.append(s[i:j + 1])
            i = j + 1
            continue
        if c in '({[':
            depth += 1
            buf.append(c)
        elif c in ')}]':
            depth -= 1
            buf.append(c)
        elif c == ',' and depth == 0:
            parts.append(''.join(buf).strip())
            buf = []
        else:
            buf.append(c)
        i += 1
    tail = ''.join(buf).strip()
    if tail:
        parts.append(tail)
    return parts


@dataclass
class Method:
    class_name: str
    name: str
    params: list
    body: str


def parse_classes(paths):
    methods = {}  # (class_name, method_name) -> Method
    consts = {}   # (class_name, field_name) -> int
    scene_methods = set()  # (class_name, method_name); names may repeat across scene classes

    method_sig_re = re.compile(
        r'(public|private|protected)?\s+static\s+(?:void|[\w<>\[\],\s]+?)\s+(\w+)\s*\(([^)]*)\)\s*\{'
    )
    const_re = re.compile(
        r'(?:private|public)?\s+static\s+final\s+int\s+(\w+)\s*=\s*(\d+)\s*;'
    )

    for path in paths:
        raw = path.read_text()
        src = strip_comments(raw)
        class_name = path.stem

        for m in const_re.finditer(src):
            consts[(class_name, m.group(1))] = int(m.group(2))

        for m in method_sig_re.finditer(src):
            modifier, name, params_raw = m.groups()
            open_brace = m.end() - 1
            close_brace = find_matching(src, open_brace)
            if close_brace == -1:
                continue
            body = src[open_brace + 1:close_brace]
            params = []
            for p in params_raw.split(','):
                p = p.strip()
                if not p:
                    continue
                params.append(p.split()[-1])  # last token = param name
            methods[(class_name, name)] = Method(class_name, name, params, body)

            # only PUBLIC static void (SceneBuilder, SceneBuildingUtil, ...) methods are actual
            # registered ponder scenes -- private helpers with the same param names (begin/lens/
            # absorbFluid/...) must not be double-counted as scenes.
            arg_types = [a.strip() for a in params_raw.split(',')]
            if modifier == 'public' and len(arg_types) >= 2 \
               and 'SceneBuilder' in arg_types[0] and 'SceneBuildingUtil' in arg_types[1]:
                scene_methods.add((class_name, name))

    return methods, consts, scene_methods


INT_EXPR_RE = re.compile(r'^[\d\s()+\-*/]+$')


def resolve_int(expr: str, env: dict, consts: dict, class_name: str):
    expr = expr.strip()
    collection_size = re.fullmatch(r'(\w+)\.(?:size\(\)|length)', expr)
    if collection_size:
        return env.get(f'__len__{collection_size.group(1)}')
    if expr in env:
        return env[expr]
    if (class_name, expr) in consts:
        return consts[(class_name, expr)]
    if re.fullmatch(r'\d+', expr):
        return int(expr)
    if INT_EXPR_RE.match(expr):
        try:
            return int(eval(expr, {"__builtins__": {}}, {}))
        except Exception:
            return None
    return None


def extract_text(args_text: str, str_env: dict = None):
    m = STRING_RE.search(args_text)
    if m:
        s = m.group(1)
        return s[:70] + ('...' if len(s) > 70 else '')
    if str_env is not None:
        key = args_text.strip()
        if key in str_env:
            s = str_env[key]
            return s[:70] + ('...' if len(s) > 70 else '')
        # concatenation like: id + ": " + suffix, or a ternary between two known vars -- best effort
        parts = [p.strip() for p in key.split('+')]
        resolved = [str_env[p] for p in parts if p in str_env]
        if resolved:
            joined = ''.join(resolved)
            return joined[:70] + ('...' if len(joined) > 70 else '')
    return None


@dataclass
class Event:
    kind: str        # 'text'
    start: int
    duration: int
    text: str
    detail: str       # e.g. 'showText' / 'showOutlineWithText'


def simulate(method: Method, methods, consts, env, cursor, events, depth=0, chain="", str_env=None):
    """Walk statements of `method`'s body, mutating `cursor` (list[0]) and appending Events."""
    if str_env is None:
        str_env = {}
    if depth > 6:
        return
    class_name = method.class_name
    for stmt in split_top_level_statements(method.body):
        s = stmt.strip()
        if not s:
            continue

        # Local collection sizes used as loop bounds. The scanner does not need the values,
        # only the number of entries in List.of(...) or an array initializer.
        m = re.match(r'^(?:final\s+)?(?:List<[^>]+>|[\w<>?]+\[\])\s+(\w+)\s*=\s*List\.of\((.*)\)$', s, re.S)
        if m:
            entries = split_top_level_commas(m.group(2))
            env[f'__len__{m.group(1)}'] = len(entries)
            continue
        m = re.match(r'^(?:final\s+)?[\w<>?]+\[\]\s+(\w+)\s*=\s*\{(.*)\}$', s, re.S)
        if m:
            entries = split_top_level_commas(m.group(2))
            env[f'__len__{m.group(1)}'] = len(entries)
            continue

        # local int variable: int NAME = EXPR;
        m = re.match(r'^(?:final\s+)?int\s+(\w+)\s*=\s*([^;]+)$', s)
        if m and '{' not in m.group(2):
            val = resolve_int(m.group(2), env, consts, class_name)
            if val is not None:
                env[m.group(1)] = val
            continue

        # local String variable: String NAME = "literal";
        m = re.match(r'^(?:final\s+)?String\s+(\w+)\s*=\s*(.+)$', s)
        if m:
            txt = extract_text(m.group(2), str_env)
            if txt is not None:
                str_env[m.group(1)] = txt
            continue

        # Enhanced for loop over a locally declared List.of(...) or array.
        m = re.match(
            r'^for\s*\(\s*(?:final\s+)?[\w<>?\[\].]+\s+(\w+)\s*:\s*(\w+)\s*\)\s*\{(.*)\}$',
            s, re.S)
        if m:
            var, collection, block = m.groups()
            iters = env.get(f'__len__{collection}')
            if iters is None:
                events.append(('unknown', f"[unresolved enhanced loop: {collection}] {chain}"))
            else:
                inner = Method(class_name, method.name + ":loop", [], block)
                for it in range(iters):
                    loop_env = dict(env)
                    loop_env[var] = it
                    simulate(inner, methods, consts, loop_env, cursor, events, depth + 1,
                             chain, dict(str_env))
            continue

        # for (int i = A; i < B; i++) { ... }   or i <= B
        m = re.match(r'^for\s*\(\s*int\s+(\w+)\s*=\s*([^;]+);\s*\1\s*(<=|<)\s*([^;]+);\s*\1\s*\+\+\s*\)\s*\{(.*)\}$', s, re.S)
        if m:
            var, start_e, cmp, end_e, block = m.groups()
            start_v = resolve_int(start_e, env, consts, class_name)
            end_v = resolve_int(end_e, env, consts, class_name)
            if start_v is not None and end_v is not None:
                iters = (end_v - start_v + 1) if cmp == '<=' else (end_v - start_v)
                iters = max(0, iters)
                inner = Method(class_name, method.name + ":loop", [], block)
                for it in range(iters):
                    loop_env = dict(env)
                    loop_env[var] = start_v + it
                    simulate(inner, methods, consts, loop_env, cursor, events, depth + 1, chain, dict(str_env))
            else:
                events.append(('unknown', f"[unresolved loop bounds: {start_e}..{end_e}] {chain}"))
            continue

        # scene.idle(EXPR)
        m = re.search(r'\.idle\(\s*([^)]*)\)', s)
        if m:
            val = resolve_int(m.group(1), env, consts, class_name)
            if val is not None:
                cursor[0] += val
            else:
                events.append(('unknown', f"[unresolved idle({m.group(1)})] {chain}"))
            continue

        # showOutlineWithText(selection, duration, "text")
        idx = s.find('.showOutlineWithText(')
        if idx != -1:
            open_p = idx + len('.showOutlineWithText')
            open_p = s.index('(', idx)
            close_p = find_matching(s, open_p)
            args = split_top_level_commas(s[open_p + 1:close_p]) if close_p != -1 else []
            if len(args) >= 3:
                dur = resolve_int(args[1], env, consts, class_name)
                text = extract_text(args[2], str_env) or extract_text(s, str_env)
                if dur is not None:
                    label = text or f"<unresolved: {args[2]}>"
                    events.append(('text', Event('text', cursor[0], dur, label, 'showOutlineWithText' + chain)))
                else:
                    events.append(('unknown', f"[unresolved showOutlineWithText dur={args[1]!r}] {chain}"))
            continue

        # showText(duration, "text")
        idx = s.find('.showText(')
        if idx != -1:
            open_p = s.index('(', idx)
            close_p = find_matching(s, open_p)
            args = split_top_level_commas(s[open_p + 1:close_p]) if close_p != -1 else []
            if len(args) >= 2:
                dur = resolve_int(args[0], env, consts, class_name)
                text = extract_text(args[1], str_env) or extract_text(s, str_env)
                if dur is not None:
                    label = text or f"<unresolved: {args[1]}>"
                    events.append(('text', Event('text', cursor[0], dur, label, 'showText' + chain)))
                else:
                    events.append(('unknown', f"[unresolved showText dur={args[0]!r}] {chain}"))
            continue

        # Call to a locally-defined or cross-class helper method that itself touches the timeline
        m = re.match(r'^(?:(\w+)\.)?(\w+)\(([^;]*)\)$', s)
        if m:
            owner, mname, args_raw = m.groups()
            args = split_top_level_commas(args_raw)
            candidates = []
            if owner:
                candidates.append((owner, mname))
            else:
                candidates.append((class_name, mname))
                # also allow same-name helpers defined in other classes (rare)
            for key in candidates:
                target = methods.get(key)
                if target is None:
                    continue
                if 'scene' not in target.params:
                    continue
                if not re.search(r'\.idle\(|\.showText\(|\.showOutlineWithText\(|[A-Za-z_]\w*\(', target.body):
                    continue
                call_env = {}
                call_str_env = {}
                for pname, pval_expr in zip(target.params, args):
                    v = resolve_int(pval_expr, env, consts, class_name)
                    if v is not None:
                        call_env[pname] = v
                    collection_arg = pval_expr.strip()
                    collection_len = env.get(f'__len__{collection_arg}')
                    if collection_len is not None:
                        call_env[f'__len__{pname}'] = collection_len
                    t = extract_text(pval_expr, str_env)
                    if t is not None:
                        call_str_env[pname] = t
                simulate(target, methods, consts, call_env, cursor, events, depth + 1,
                         chain + f" <- {mname}()", call_str_env)
                break
    return


def overlaps(a: Event, b: Event):
    return a.start < (b.start + b.duration) and b.start < (a.start + a.duration)


def main():
    paths = sorted(SCENES_DIR.glob("*.java"))
    methods, consts, scene_methods = parse_classes(paths)

    total_scenes = 0
    total_overlaps = 0
    unresolved_total = 0

    for class_name, scene_name in sorted(scene_methods):
        method = methods[(class_name, scene_name)]
        cursor = [0]
        raw_events = []
        simulate(method, methods, consts, {}, cursor, raw_events, chain=f":{scene_name}")
        total_scenes += 1

        text_events = [e for kind, e in raw_events if kind == 'text']
        unknowns = [msg for kind, msg in raw_events if kind == 'unknown']
        unresolved_total += len(unknowns)

        text_events.sort(key=lambda e: e.start)
        found = []
        for i in range(len(text_events)):
            for j in range(i + 1, len(text_events)):
                if overlaps(text_events[i], text_events[j]):
                    found.append((text_events[i], text_events[j]))

        if found or unknowns:
            print(f"\n=== {class_name}.{scene_name} ===")
            for a, b in found:
                total_overlaps += 1
                print(f"  OVERLAP: [{a.start}-{a.start + a.duration}] {a.detail}: \"{a.text}\"")
                print(f"       vs  [{b.start}-{b.start + b.duration}] {b.detail}: \"{b.text}\"")
            for u in unknowns:
                print(f"  ?? {u}")

    print(f"\n--- Scanned {total_scenes} scenes, {total_overlaps} overlapping text windows, "
          f"{unresolved_total} unresolved spots (check manually) ---")


if __name__ == '__main__':
    main()
