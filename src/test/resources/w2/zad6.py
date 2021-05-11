import queue
import heapq

class state:
    def __init__(self, positions, moves):
        self.positions = positions
        self.moves = moves

    def __eq__(self, other):
        return self.__hash__() == other.__hash__()

    def __hash__(self):
        return hash(tuple(self.positions))

    def __lt__(self, other):
        return len(self.moves) < len(other.moves)

    def is_done(self):
        return all([x in goals for x in self.positions])

    def gen_next_states(self):
        adj = []
        for i in range(4):
            ps = set()
            ms = ''
            for p in self.positions:
                x = p[0] + dx[i]
                y = p[1] + dy[i]
                if (x, y) not in walls:
                    ps.add((x, y))
                else:
                    ps.add(p)
            ms += mv[i]
            adj_state = state(ps, self.moves + ms)
            adj.append(adj_state)
        return adj

    def make_move(self,q):
        ps = set()
        for pos in self.positions:
            x = pos[0] + dx[q]
            y = pos[1] + dy[q]
            if (x, y) not in walls:
                ps.add((x, y))
            else:
                ps.add(pos)
        self.moves += mv[q]
        self.positions = ps

    def heur(self):
        ds = [dist[p] for p in self.positions]
        return VALUE*max(ds) + len(self.moves)

def find_dist(p):
    q = queue.Queue()
    visited = set()
    q.put((p, 0))
    visited.add(p)
    while not q.empty():
        currp, currd = q.get()
        if currp in goals:
            return currd
        for i in range(4):
            newp = (currp[0] + dx[i], currp[1] + dy[i])
            if newp not in walls and newp not in visited:
                newd = currd + 1
                visited.add(newp)
                q.put((newp, newd))

def init(b):
    positions = set()
    n = len(b)
    m = len(b[0])
    for i in range(n):
        for j in range(m):
            if b[i][j] == 'S':
                positions.add((i, j))
            elif b[i][j] == 'G':
                goals.add((i, j))
            elif b[i][j] == 'B':
                positions.add((i, j))
                goals.add((i, j))
            elif b[i][j] == '#':
                walls.add((i, j))
    return state(positions, "")


def bfs(s):
    visited = set()
    q = queue.Queue()
    lowest_pos_no = len(s.positions)
    q.put(s)
    visited.add(s)
    while not q.empty():
        curr = q.get()
        if curr.is_done():
            return curr.moves
        for adj in curr.gen_next_states():
            if adj not in visited:
                if len(adj.positions) < lowest_pos_no:
                    visited.clear()
                    q.queue.clear()
                    lowest_pos_no = len(adj.positions)
                    visited.add(adj)
                    q.put(adj)
                    break
                visited.add(adj)
                q.put(adj)


def a_star(s):
    visited = set()
    q = []
    heapq.heappush(q, (s.heur(), s))
    visited.add(s)
    while len(q) > 0:
        _, curr = heapq.heappop(q)
        if curr.is_done():
            return curr.moves
        for adj in curr.gen_next_states():
            if adj not in visited:
                visited.add(adj)
                heapq.heappush(q, (adj.heur(), adj))

def a_star2(s):
    steps = 500
    visited = set()
    q = []
    heapq.heappush(q, (s.heur(), s))
    visited.add(s)
    while len(q) > 0:
        _, curr = heapq.heappop(q)
        if curr.is_done() or steps == 0:
            return curr
        steps -= 1
        for adj in curr.gen_next_states():
            if adj not in visited:
                visited.add(adj)
                heapq.heappush(q, (adj.heur(), adj))

def start(data):
    goals.clear()
    walls.clear()
    init_state = init(data)

    # for _ in range(len(data)-2):
    #     init_state.make_move(1)
    # for _ in range(len(data[0])-2):
    #     init_state.make_move(3)
    # final_state = bfs(init_state)



    for i in range(len(data)):
        for j in range(len(data[0])):
            if (i, j) not in walls:
                 dist[(i, j)] = find_dist((i, j))
    final_state = a_star(init_state)



    # for i in range(len(data)):
    #     for j in range(len(data[0])):
    #         if (i, j) not in walls:
    #             dist[(i, j)] = find_dist((i, j))
    # final_state = a_star2(init_state)
    # final_state = bfs(final_state)
    #
    out.write(final_state)
    out.write('\n')


with open('zad_input.txt', 'r') as f:
    data = f.read().strip().split('\n')

out = open('zad_output.txt', 'w')

VALUE = 1.5

dx = [-1, 1, 0, 0]
dy = [0, 0, 1, -1]
mv = {0: 'U', 1: 'D', 2: 'R', 3: 'L'}

walls = set()
goals = set()

dist ={}

start(data)
out.close()
