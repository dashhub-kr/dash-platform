SET FOREIGN_KEY_CHECKS = 0; -- Disable foreign key checks to allow truncation

-- =====================================================
-- DASH Mock Data Seed Script (Idempotent / Re-runnable)
-- 발표용 데모 데이터 (Circular Dependency Fixed + Clean Start)
-- 생성일: 2025-12-25
-- Updated: 2025-12-26
-- =====================================================

-- =====================================================
-- 0. 기존 데이터 초기화 (TRUNCATE)
-- =====================================================
TRUNCATE TABLE study_mission_submissions;
TRUNCATE TABLE study_applications;
TRUNCATE TABLE comments;
TRUNCATE TABLE boards;
TRUNCATE TABLE study_missions;
TRUNCATE TABLE users;
TRUNCATE TABLE studies;

-- =====================================================
-- 1. 유저 데이터 (총 15명) - study_id는 NULL로 먼저 생성
-- =====================================================
-- (사용자 요청으로 목 데이터 비활성화 - 2026-01-07)
-- INSERT INTO users (username, email, solvedac_handle, solvedac_tier, solvedac_rating, solvedac_class, solved_count, stats_last_synced_at, silver_streak, gold_streak, max_silver_streak, max_gold_streak, created_at, provider, provider_id, avatar_url, study_id)
-- VALUES
-- -- 기존 유저 (1-4)
-- ('algorithm_master', 'algo_master@github.placeholder', 'algo_master', 14, 1350, 4, 287, NOW(), 5, 3, 10, 5, DATE_SUB(NOW(), INTERVAL 30 DAY), 'github', '12345678', 'https://api.dicebear.com/7.x/adventurer/svg?seed=AlgoMaster', NULL),
-- ('code_ninja', 'code_ninja@github.placeholder', 'code_ninja', 10, 750, 3, 156, NOW(), 3, 0, 7, 2, DATE_SUB(NOW(), INTERVAL 25 DAY), 'github', '23456789', 'https://api.dicebear.com/7.x/adventurer/svg?seed=CodeNinja', NULL),
-- ('dp_expert', 'dp_expert@github.placeholder', 'dp_expert', 13, 1150, 4, 234, NOW(), 7, 4, 15, 8, DATE_SUB(NOW(), INTERVAL 20 DAY), 'github', '34567890', 'https://api.dicebear.com/7.x/adventurer/svg?seed=DPExpert', NULL),
-- ('graph_hero', 'graph_hero@github.placeholder', 'graph_hero', 16, 1620, 5, 412, NOW(), 10, 7, 20, 12, DATE_SUB(NOW(), INTERVAL 15 DAY), 'github', '45678901', 'https://api.dicebear.com/7.x/adventurer/svg?seed=GraphHero', NULL),
-- 
-- -- 신규 유저 (5-15)
-- ('greedy_king', 'greedy@github.placeholder', 'greedy_king', 18, 1850, 6, 620, NOW(), 15, 12, 30, 20, DATE_SUB(NOW(), INTERVAL 60 DAY), 'github', '56789012', 'https://api.dicebear.com/7.x/adventurer/svg?seed=GreedyKing', NULL),
-- ('bfs_master', 'bfs@github.placeholder', 'bfs_master', 14, 1380, 4, 310, NOW(), 6, 2, 12, 5, DATE_SUB(NOW(), INTERVAL 40 DAY), 'github', '67890123', 'https://api.dicebear.com/7.x/adventurer/svg?seed=BFSMaster', NULL),
-- ('newbie_coder', 'newbie@github.placeholder', 'newbie_coder', 4, 120, 1, 30, NOW(), 1, 0, 2, 0, DATE_SUB(NOW(), INTERVAL 5 DAY), 'github', '78901234', 'https://api.dicebear.com/7.x/adventurer/svg?seed=NewbieCoder', NULL),
-- ('full_stack', 'fullstack@github.placeholder', 'full_stack', 9, 650, 2, 120, NOW(), 4, 1, 8, 2, DATE_SUB(NOW(), INTERVAL 45 DAY), 'github', '89012345', 'https://api.dicebear.com/7.x/adventurer/svg?seed=FullStack', NULL),
-- ('kakao_intern', 'kakao@github.placeholder', 'kakao_intern', 15, 1480, 5, 380, NOW(), 8, 5, 18, 8, DATE_SUB(NOW(), INTERVAL 90 DAY), 'github', '90123456', 'https://api.dicebear.com/7.x/adventurer/svg?seed=KakaoIntern', NULL),
-- ('samsung_man', 'samsung@github.placeholder', 'samsung_man', 17, 1720, 5, 550, NOW(), 12, 8, 25, 15, DATE_SUB(NOW(), INTERVAL 100 DAY), 'github', '01234567', 'https://api.dicebear.com/7.x/adventurer/svg?seed=SamsungMan', NULL),
-- ('algorithm_god', 'god@github.placeholder', 'algorithm_god', 22, 2300, 7, 1200, NOW(), 50, 40, 100, 80, DATE_SUB(NOW(), INTERVAL 365 DAY), 'github', '11223344', 'https://api.dicebear.com/7.x/adventurer/svg?seed=AlgoGod', NULL),
-- ('python_lover', 'py@github.placeholder', 'python_lover', 8, 550, 2, 90, NOW(), 2, 0, 5, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 'github', '22334455', 'https://api.dicebear.com/7.x/adventurer/svg?seed=PyLover', NULL),
-- ('java_wizard', 'java@github.placeholder', 'java_wizard', 13, 1280, 4, 250, NOW(), 5, 3, 10, 4, DATE_SUB(NOW(), INTERVAL 28 DAY), 'github', '33445566', 'https://api.dicebear.com/7.x/adventurer/svg?seed=JavaWizard', NULL),
-- ('cpp_warrior', 'cpp@github.placeholder', 'cpp_warrior', 19, 1950, 6, 780, NOW(), 20, 15, 35, 22, DATE_SUB(NOW(), INTERVAL 150 DAY), 'github', '44556677', 'https://api.dicebear.com/7.x/adventurer/svg?seed=CppWarrior', NULL),
-- ('data_scientist', 'ds@github.placeholder', 'data_scientist', 11, 850, 3, 180, NOW(), 3, 1, 6, 2, DATE_SUB(NOW(), INTERVAL 35 DAY), 'github', '55667788', 'https://api.dicebear.com/7.x/adventurer/svg?seed=DataScientist', NULL);

-- =====================================================
-- 2. 스터디 그룹 데이터 (총 5개)
-- =====================================================
INSERT INTO studies (name, creator_id, created_at, acorn_count, visibility, description, streak, active_mission_title)
VALUES
-- 기존 스터디 (1-2)
('SSAFY 알고리즘 마스터', 1, DATE_SUB(NOW(), INTERVAL 14 DAY), 25, 'PUBLIC', '싸피 12기 알고리즘 스터디입니다. 매주 5문제씩 풀고 서로 코드리뷰해요!', 14, 'DP 심화 문제풀이'),
('취준생 코테 스터디', 4, DATE_SUB(NOW(), INTERVAL 7 DAY), 12, 'PUBLIC', '코딩테스트 준비하는 취준생들 모임. 삼성, 카카오, 네이버 기출 위주로!', 7, '그리디 & 이분탐색'),
-- 신규 스터디 (3-5)
('백준 골드 달성반', 6, DATE_SUB(NOW(), INTERVAL 30 DAY), 150, 'PUBLIC', '실버 탈출하고 골드 찍어봅시다! 기초부터 탄탄하게.', 25, 'BFS/DFS 기초 다지기'),
('삼성 상시 테스트 대비', 10, DATE_SUB(NOW(), INTERVAL 60 DAY), 320, 'PUBLIC', 'A형, B형 취득을 목표로 하는 스터디입니다. 빡세게 하실 분만.', 45, '구현 & 시뮬레이션 끝판왕'),
('Python 알고리즘 클럽', 12, DATE_SUB(NOW(), INTERVAL 5 DAY), 8, 'PUBLIC', '파이썬으로 코테 준비하는 사람들의 모임. 파이써닉한 코드 공유해요.', 3, 'Python 자료구조 활용');

-- =====================================================
-- 2-1. 유저 스터디 배정 (UPDATE)
-- =====================================================
-- 스터디 1: 1, 2, 3
UPDATE users SET study_id = 1 WHERE username IN ('algorithm_master', 'code_ninja', 'dp_expert');

-- 스터디 2: 4, 5
UPDATE users SET study_id = 2 WHERE username IN ('graph_hero', 'greedy_king');

-- 스터디 3: 6, 7, 8
UPDATE users SET study_id = 3 WHERE username IN ('bfs_master', 'newbie_coder', 'full_stack');

-- 스터디 4: 9, 10
UPDATE users SET study_id = 4 WHERE username IN ('kakao_intern', 'samsung_man');

-- 스터디 5: 12, 13
UPDATE users SET study_id = 5 WHERE username IN ('python_lover', 'java_wizard');

-- 스터디 없음: 11, 14, 15 (algorithm_god, cpp_warrior, data_scientist)


-- =====================================================
-- 3. 스터디 미션 (총 12개)
-- =====================================================
INSERT INTO study_missions (study_id, week, title, problem_ids, source_type, deadline, status, created_at)
VALUES
-- 스터디1 (SSAFY Alg Master)
(1, 4, '그래프 탐색 기초', '[1260,2606,11724,2667]', 'CURRICULUM', DATE_ADD(NOW(), INTERVAL 7 DAY), 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 5, '최단경로 알고리즘', '[1753,1916,11404,1504]', 'CURRICULUM', DATE_ADD(NOW(), INTERVAL 14 DAY), 'NOT_STARTED', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 스터디2 (취준생)
(2, 1, '삼성 SW 역량테스트 기출', '[14500,14501,14502,14503]', 'MANUAL', DATE_ADD(NOW(), INTERVAL 5 DAY), 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 2, '카카오 2024 블라인드 기출', '[258712,258709,258707]', 'MANUAL', DATE_ADD(NOW(), INTERVAL 12 DAY), 'NOT_STARTED', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 스터디3 (골드 달성반)
(3, 10, '그리디 알고리즘 정복', '[11047,1931,11399,1541]', 'CURRICULUM', DATE_SUB(NOW(), INTERVAL 1 DAY), 'COMPLETED', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, 11, '이분탐색의 이해', '[1920,2805,1654,2110]', 'CURRICULUM', DATE_ADD(NOW(), INTERVAL 6 DAY), 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 1 DAY)),

-- 스터디4 (삼성 대비)
(4, 8, '시뮬레이션 심화 1', '[15686,14891,14503,3190]', 'MANUAL', DATE_SUB(NOW(), INTERVAL 2 DAY), 'COMPLETED', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 9, '시뮬레이션 심화 2', '[16234,17144,17140,17837]', 'MANUAL', DATE_ADD(NOW(), INTERVAL 5 DAY), 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 2 DAY)),

-- 스터디5 (Python 클럽)
(5, 1, '문자열 처리와 파이썬', '[9935,5052,1120,5430]', 'CURRICULUM', DATE_ADD(NOW(), INTERVAL 4 DAY), 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- =====================================================
-- 4. 스터디 미션 제출 현황
-- =====================================================
INSERT INTO study_mission_submissions (mission_id, user_id, problem_id, completed, is_sos, completed_at)
VALUES
-- 미션4 (그래프 탐색) 제출 (스터디 1)
(1, 1, 1260, 1, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 1, 2606, 1, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 2, 1260, 1, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 3, 1260, 1, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 3, 2606, 1, 0, NOW()),
(1, 3, 11724, 1, 0, NOW()),
(1, 4, 1260, 0, 1, NULL), -- SOS

-- 미션3 (삼성 기출) 제출 (스터디 2), Mission 3 corresponds to auto-increment ID
(3, 3, 14500, 1, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 3, 14501, 1, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 4, 14500, 1, 0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 4, 14501, 1, 0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 4, 14502, 1, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 5, 14500, 1, 0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 5, 14503, 1, 0, NOW()),

-- Mission 5 (Greedy) -> Study 3
(5, 6, 11047, 1, 0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(5, 6, 1931, 1, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 7, 11047, 1, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 7, 11399, 1, 0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 8, 11047, 1, 0, DATE_SUB(NOW(), INTERVAL 5 DAY)),

-- Mission 7 (Simul 1) -> Study 4
(7, 9, 15686, 1, 0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(7, 9, 14891, 1, 0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(7, 10, 15686, 1, 0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(7, 10, 14891, 1, 0, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(7, 10, 14503, 1, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(7, 10, 3190, 1, 0, DATE_SUB(NOW(), INTERVAL 5 DAY));


-- =====================================================
-- 5. 게시글 (총 30개)
-- =====================================================
INSERT INTO boards (title, content, user_id, algorithm_record_id, board_type, like_count, created_at, updated_at)
VALUES
-- 인기글 (High Likes)
('코딩테스트 시간복잡도 계산 팁', '- 1초 = 약 10^8 연산\n- N ≤ 10: O(N!)\n- N ≤ 20: O(2^N)\n- N ≤ 500: O(N³)\n- N ≤ 5000: O(N²)\n- N ≤ 100000: O(NlogN)\n- N ≤ 10000000: O(N)\n\n이거 기억하면 문제 보고 어떤 알고리즘 써야할지 감이 와요!', 10, NULL, 'GENERAL', 55, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
('카카오 코테 합격 후기 (feat. 블라인드)', '드디어 카카오 코테 통과했습니다! 총 7문제 중 5문제 완벽, 2문제 부분 점수로 합격했어요.\n\n핵심 팁:\n1. 문제 꼼꼼히 읽기\n2. 테스트케이스 검증\n3. 효율적인 시간 분배', 9, NULL, 'GENERAL', 67, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
('이분탐색 정복 방법', '# 이분탐색 마스터하기\n\n1. 결정 문제로 바꾸기: "X 이하로 가능한가?"\n2. 단조성 확인하기: X가 가능하면 X+1도 가능?\n3. 경계값 처리 신경쓰기: lo, hi, mid 설정\n\n연습 문제: 2805, 1654, 2110', 5, NULL, 'GENERAL', 41, NOW(), NOW()),
('삼성 SW 역량테스트 후기', '오늘 삼성 SW 역량테스트 봤는데 시뮬레이션 2문제 나왔어요! 구현력이 정말 중요한 것 같습니다. 특히 시간 관리가 핵심이에요.', 10, NULL, 'GENERAL', 35, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('그래프 알고리즘 정리 (BFS/DFS/다익스트라)', '면접에서 자주 나오는 그래프 알고리즘들 정리했습니다!\n\n**BFS**: O(V+E), 최단거리\n**DFS**: O(V+E), 경로탐색\n**다익스트라**: O(ElogV), 최단경로', 6, NULL, 'GENERAL', 38, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),

-- 일반글 / 질문
('알고리즘 스터디 같이 하실 분!', '안녕하세요! 골드 목표로 달리실 분 구합니다.', 3, NULL, 'GENERAL', 12, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('DP 점화식 팁 공유합니다', '1. 문제를 작은 부분으로 나누기\n2. 점화식 세우기\n3. 기저 조건 정하기', 5, NULL, 'GENERAL', 28, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('BFS vs DFS 언제 뭘 써야 할까요?', '최단거리나 가중치 없을땐 BFS, 경로탐색은 DFS가 편한 것 같아요.', 3, NULL, 'GENERAL', 18, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('Python vs C++ 코테 언어 추천 좀', 'Python이 편하긴 한데 시간초과 걱정이네요. C++로 갈아탈까요?', 12, NULL, 'GENERAL', 22, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('Union-Find 활용 문제', '1717, 1976, 4195 풀어보세요. 경로 압축 꼭 하시구요!', 5, NULL, 'GENERAL', 29, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
('슬라이딩 윈도우 개념 질문', '고정된 윈도우 크기랑 가변 윈도우 크기 차이가 뭔가요?', 7, NULL, 'GENERAL', 5, DATE_SUB(NOW(), INTERVAL 12 HOUR), DATE_SUB(NOW(), INTERVAL 12 HOUR)),
('1753번 최단경로 질문입니다', '다익스트라 구현했는데 시간초과 나네요. 우선순위 큐 썼는데 왜일까요?\n\n(코드 첨부 생략)', 7, NULL, 'GENERAL', 3, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('그리디 알고리즘 증명하는 법', '그리디가 항상 최적해를 보장한다는 걸 어떻게 증명하나요? 직관적으로는 알겠는데...', 8, NULL, 'GENERAL', 8, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
('소프티어 인증 후기', '레벨3 땄습니다! 생각보다 구현이 빡세네요.', 13, NULL, 'GENERAL', 15, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('백준 티어 올리기 좋은 문제들', '실버에서 골드 가실 분들 1000~2000번대 클래식 문제부터 푸세요.', 11, NULL, 'GENERAL', 45, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
('투 포인터 언제 쓰나요?', '정렬된 배열에서 두 수의 합 같은 거 구할 때 씁니다. O(N)으로 줄일 수 있어요.', 14, NULL, 'GENERAL', 19, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('LCA 알고리즘 이해가 안가요', '최소 공통 조상... 희소 배열이 뭔가요?', 8, NULL, 'GENERAL', 6, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('세그먼트 트리 꼭 해야하나요?', '코테 준비중인데 세그먼트 트리까지 해야하는지 모르겠어요. 너무 어려움 ㅠ', 12, NULL, 'GENERAL', 11, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
('재귀함수 깊이 제한', '파이썬 sys.setrecursionlimit() 다들 얼마로 설정하시나요?', 13, NULL, 'GENERAL', 7, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
('문자열 매칭 KMP 알고리즘', '접두사/접미사 일치 길이 배열 만드는 게 핵심입니다.', 14, NULL, 'GENERAL', 20, DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY)),
('삼성 A형 기출 리스트', '백준 그룹에 정리해뒀습니다. 링크 참고하세요!', 10, NULL, 'GENERAL', 51, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
('SQL 코테 대비는 어떻게?', '프로그래머스 고득점 Kit 추천합니다.', 15, NULL, 'GENERAL', 30, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
('벨만포드 질문', '음수 사이클이 존재하면 최단경로를 구할 수 없나요?', 8, NULL, 'GENERAL', 4, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY)),
('위상정렬 선수과목 문제', '진입차수 0인거 큐에 넣고 돌리면 됩니다.', 6, NULL, 'GENERAL', 14, '2025-12-20', '2025-12-20'),
('플로이드 워셜 O(V^3)인데', 'N이 500 이하면 쓸 수 있습니다.', 5, NULL, 'GENERAL', 9, DATE_SUB(NOW(), INTERVAL 13 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY)),
('비트마스킹 활용 팁', '집합 표현할 때 배열 대신 정수 하나로! 메모리 절약 개꿀', 10, NULL, 'GENERAL', 23, DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),
('CCW 알고리즘', '기하 문제의 기초. 세 점의 방향성을 알 수 있어요.', 11, NULL, 'GENERAL', 17, DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
('JS로 코테 보시는 분?', '입출력 처리가 너무 귀찮은데 템플릿 있으신가요?', 15, NULL, 'GENERAL', 8, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('백트래킹 가지치기 팁', '유망하지 않은 경우 빠르게 return 하는 게 핵심입니다.', 9, NULL, 'GENERAL', 33, DATE_SUB(NOW(), INTERVAL 19 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY)),
('2025년 코테 트렌드 예상', '구현력이 점점 더 중요해지는 것 같습니다. 기본기 탄탄하게!', 11, NULL, 'GENERAL', 60, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY));

-- =====================================================
-- 6. 댓글 데이터
-- =====================================================
INSERT INTO comments (board_id, user_id, parent_id, line_number, content, like_count, created_at, updated_at)
VALUES
-- 게시글 1 (시간복잡도)
(1, 6, NULL, NULL, '정말 유용한 정보네요 감사합니다!', 5, NOW(), NOW()),
(1, 8, NULL, NULL, '항상 헷갈렸는데 정리 잘해주셨네요.', 3, NOW(), NOW()),

-- 게시글 2 (카카오 후기)
(2, 5, NULL, NULL, '축하드립니다! 저도 열심히 해야겠어요.', 10, NOW(), NOW()),
(2, 7, NULL, NULL, '부럽습니다 ㅠㅠ 전 3문제밖에 못 풀었어요.', 2, NOW(), NOW()),

-- 게시글 4 (삼성 후기)
(4, 9, NULL, NULL, '저도 봤는데 2번 문제가 까다롭더라구요.', 4, NOW(), NOW()),
(4, 11, 5, NULL, '맞아요 회전시키는 게 너무 복잡했어요.', 2, NOW(), NOW()),

-- 게시글 9 (Python vs C++)
(9, 10, NULL, NULL, 'C++이 확실히 빠르긴 하죠. 근데 생산성은 파이썬 압승.', 8, NOW(), NOW()),
(9, 14, NULL, NULL, 'PyPy3로 제출하면 웬만하면 통과됩니다!', 6, NOW(), NOW()),

-- 게시글 11 (슬라이딩 윈도우)
(11, 5, NULL, NULL, '고정은 창문 크기가 그대로 이동, 가변은 조건에 따라 창문 크기가 늘었다 줄었다 해요.', 7, NOW(), NOW()),

-- 게시글 12 (1753 질문)
(12, 6, NULL, NULL, 'visited 배열 체크 위치 확인해보세요. 큐에서 뺄 때 체크해야 합니다.', 3, NOW(), NOW()),

-- 게시글 13 (그리디 증명)
(13, 11, NULL, NULL, '귀류법이나 수학적 귀납법 많이 써요. "최적해가 아니라고 가정해보자..." 로 시작.', 5, NOW(), NOW()),

-- 게시글 15 (백준 티어)
(15, 3, NULL, NULL, '실버3~1 구간 문제들이 기본기 다지기 좋음.', 9, NOW(), NOW()),
(15, 4, 12, NULL, '동의합니다. 거기서 BFS, DFS, DP 다 경험해볼 수 있죠.', 4, NOW(), NOW());

-- =====================================================
-- 7. 스터디 신청 (Applications)
-- =====================================================
INSERT INTO study_applications (study_id, user_id, message, status, created_at)
VALUES
(1, 7, '열심히 하겠습니다 받아주세요!', 'PENDING', NOW()),
(2, 8, '취준생입니다. 간절합니다.', 'PENDING', NOW()),
(3, 13, '골드 찍고 싶어요 ㅠㅠ', 'PENDING', NOW()),
(4, 5, '삼성맨이 되고 싶습니다.', 'PENDING', NOW());

-- =====================================================
-- 완료 메시지
-- =====================================================
SELECT 'Mock Data Seed Completed with Truncate!' AS result;

SET FOREIGN_KEY_CHECKS = 1; -- Re-enable foreign key checks
