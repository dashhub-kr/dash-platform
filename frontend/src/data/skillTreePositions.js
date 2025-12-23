/**
 * 스킬 트리 수동 배치 - 광활한 간격 배치 (겹침 완전 방지)
 * 
 * 캔버스: 약 2000 x 1800px
 * 핵심 허브 간격: 300~400px
 * 노드 간 최소 거리: 200px 이상 유지
 */

// 랜덤 오프셋 헬퍼 (±20px)
const r = () => (Math.random() - 0.5) * 40;

// 수동 배치된 태그 좌표 (중앙 집중형 수정)
// 캔버스 중심: 약 (1000, 900) 기준

export const manualPositions = {
    // ============================================
    // 중심 허브: 6대 핵심 태그 (더 모음)
    // ============================================
    'dp': { x: 1000, y: 700 },          // 중앙 상단
    'graphs': { x: 1250, y: 800 },      // 중앙 우측
    'implementation': { x: 1200, y: 1050 }, // 중앙 우하단
    'math': { x: 1000, y: 1150 },       // 중앙 하단
    'sorting': { x: 750, y: 1050 },     // 중앙 좌하단
    'data_structures': { x: 682, y: 922 }, // 중앙 좌측 (핵심 허브로 격상)
    'string': { x: 750, y: 800 },       // 중앙 좌상단

    // ============================================
    // DP 계열 (북쪽 - 위로)
    // ============================================
    'recursion': { x: 1040, y: 580 },
    'bruteforcing': { x: 850, y: 600 },
    'greedy': { x: 1150, y: 600 },
    'knapsack': { x: 904, y: 490 },
    'lis': { x: 1100, y: 490 },
    'dp_tree': { x: 800, y: 400 },
    'dp_bitfield': { x: 1200, y: 400 },

    // ============================================
    // 그래프 계열 (동쪽 - 오른쪽으로)
    // ============================================
    'bfs': { x: 1400, y: 750 },
    'dfs': { x: 1500, y: 850 },
    'trees': { x: 1400, y: 950 },
    'graph_traversal': { x: 1600, y: 800 },

    // 그래프 상세
    'dijkstra': { x: 1550, y: 600 }, // 650 -> 600 (위로)
    'shortest_path': { x: 1700, y: 650 }, // 700 -> 650 (위로)
    'mst': { x: 1500, y: 1000 }, // 1550 -> 1500 (약간 왼쪽)
    'topological_sorting': { x: 1650, y: 900 }, // 1700 -> 1650 (왼쪽)
    'floyd_warshall': { x: 1800, y: 550 }, // 1750 -> 1800, 600 -> 550 (우상향)
    'graph_traversal': { x: 1600, y: 750 }, // 800 -> 750 (위로, SCC와 분리)

    // 그래프 고급
    'lca': { x: 1650, y: 1100 }, // 1600 -> 1650 (우측), 1050 -> 1100 (아래)
    'scc': { x: 1800, y: 850 }, // 800 -> 850 (아래로)
    'bellman_ford': { x: 1850, y: 650 },
    'euler_tour_technique': { x: 1750, y: 1150 }, // 1050 -> 1150 (더 아래로)
    'bipartite_matching': { x: 1850, y: 950 }, // 1800 -> 1850 (우측), 900 -> 950 (아래)
    'mf': { x: 1900, y: 750 },
    'mcmf': { x: 2000, y: 850 },

    // ============================================
    // 구현/시뮬레이션 계열 (동남 - 오른쪽 아래)
    // ============================================
    'simulation': { x: 1350, y: 1150 },
    'backtracking': { x: 1450, y: 1250 },
    'divide_and_conquer': { x: 1250, y: 1250 },

    'ad_hoc': { x: 1400, y: 1350 },
    'constructive': { x: 1550, y: 1300 },
    'heuristics': { x: 1600, y: 1200 },
    'mo': { x: 1500, y: 1150 },

    // ============================================
    // 수학 계열 (남쪽 - 아래로)
    // ============================================
    'number_theory': { x: 900, y: 1300 },
    'combinatorics': { x: 1100, y: 1300 },
    'geometry': { x: 1000, y: 1400 },

    // 수학 상세
    'probability': { x: 1200, y: 1400 },
    'primality_test': { x: 800, y: 1400 },
    'sieve': { x: 850, y: 1500 },
    'euclidean': { x: 750, y: 1350 },
    'arithmetic': { x: 700, y: 1250 },
    'convex_hull': { x: 1000, y: 1550 },
    'fft': { x: 1150, y: 1500 },
    'linear_algebra': { x: 600, y: 1300 },
    'calculus': { x: 900, y: 1600 },

    // ============================================
    // 정렬/탐색 계열 (서남 - 왼쪽 아래)
    // ============================================
    'binary_search': { x: 640, y: 1140 },
    'two_pointer': { x: 500, y: 1050 },
    'sliding_window': { x: 450, y: 1150 },
    'priority_queue': { x: 550, y: 950 }, // 자료구조 근처

    'offline_queries': { x: 350, y: 1100 },
    'ternary_search': { x: 500, y: 1250 },
    'parametric_search': { x: 650, y: 1250 },

    // ============================================
    // 문자열 계열 (서북 - 왼쪽 위)
    // ============================================
    'kmp': { x: 600, y: 750 },
    'trie': { x: 550, y: 650 },

    'hashing': { x: 500, y: 720 }, // 800 -> 720 (올림)
    'parsing': { x: 450, y: 650 }, // 700 -> 650 (올림)
    'string_matching': { x: 650, y: 600 },

    // ============================================
    // 자료구조 계열 (서쪽 - 왼쪽)
    // ============================================
    'stack': { x: 450, y: 900 },
    'queue': { x: 450, y: 980 },
    'deque': { x: 350, y: 940 },
    'set_map': { x: 400, y: 850 },

    'segtree': { x: 200, y: 850 }, // 250 -> 200 (왼쪽으로)
    'disjoint_set': { x: 300, y: 1050 }, // 1000 -> 1050 (아래로)
    'hash_set': { x: 363, y: 775 }, // 350 -> 300 (왼쪽으로)
    'tree_set': { x: 150, y: 700 }, // 250 -> 150 (더 왼쪽으로)
    'bitmask': { x: 250, y: 550 }, // 300 -> 250 (왼쪽으로, 650 -> 550 위로)
};

/**
 * 태그 위치 반환 (랜덤성 추가)
 */
export const getTagPosition = (tagKey, familyId, index) => {
    // 랜덤 오프셋 제거 (정확한 위치 사용)
    if (manualPositions[tagKey]) {
        return manualPositions[tagKey];
    }

    // 비상용 자동 배치 (발생 안해야 함)
    const col = (index || 0) % 5;
    const row = Math.floor((index || 0) / 5);

    return {
        x: 100 + col * 250,
        y: 1800 + row * 180
    };
};

/**
 * 모든 태그에 대한 위치 계산
 */
export const calculateAllPositions = (nodes) => {
    if (!nodes) return {};

    const familyCounters = {};
    const positions = {};

    nodes.forEach(node => {
        const familyId = node.familyId || 0;
        if (!familyCounters[familyId]) familyCounters[familyId] = 0;

        positions[node.id] = getTagPosition(
            node.id,
            familyId,
            familyCounters[familyId]
        );

        if (!manualPositions[node.id]) {
            familyCounters[familyId]++;
        }
    });

    return positions;
};

export default {
    manualPositions,
    getTagPosition,
    calculateAllPositions
};
