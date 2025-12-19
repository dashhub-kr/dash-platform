logo
@solvedac/unofficial-documentation
Endpoints
Schemas
powered by Stoplight
클래스별로 사용자가 푼 문제 수 가져오기
get
https://solved.ac/api/v3
/user/class_stats
해당 핸들의 사용자가 푼 문제 수를 클래스별로 나누어 가져옵니다.

Request
Query Parameters
handle
string
required
요청할 사용자명

Responses
200
클래스별 푼 문제 수가 담긴 목록

Body

application/json

application/json
array of:
class
number
required
클래스 번호입니다.

Allowed values:
1
2
3
4
5
6
7
8
9
10
Example:
1
total
integer
required
solved.ac에 등록된 해당 클래스의 문제 수입니다.

Example:
36
totalSolved
integer
required
사용자가 푼 클래스 문제 수입니다.

Example:
36
essentials
integer
required
solved.ac에 등록된 해당 클래스의 에센셜 문제 수입니다.

Example:
16
essentialSolved
integer
required
사용자가 푼 클래스 에센셜 문제 수입니다.

Example:
16
decoration
string
required
사용자가 획득한 클래스 치장입니다.

Example:
gold
handle*
:
string
Send API Request
curl --request GET \
  --url https://solved.ac/api/v3/user/class_stats \
  --header 'Accept: application/json'
[
  {
    "class": 1,
    "total": 36,
    "totalSolved": 36,
    "essentials": 16,
    "essentialSolved": 16,
    "decoration": "gold"
  }
]


logo
@solvedac/unofficial-documentation
Endpoints
Schemas
powered by Stoplight
태그별로 사용자가 푼 문제 수 가져오기
get
https://solved.ac/api/v3
/user/problem_tag_stats
해당 핸들의 사용자가 푼 문제 수를 태그별로 나누어 가져옵니다.

Request
Query Parameters
handle
string
required
요청할 사용자명

Responses
200
태그별 푼 문제 수가 담긴 목록

Body

application/json

application/json
페이지네이션 가능한 쿼리의 응답 결과입니다.

count
integer
required
전체 원소 수입니다.

Example:
1
items
array[GetUserProblemTagStats.ProblemTagStat]
(go to ref)
required
현재 페이지의 원소 목록입니다.

tag
object
required
total
integer
required
solved.ac에 등록된 해당 태그 문제 수입니다.

Example:
0
solved
integer
required
사용자가 푼 문제 수입니다.

partial
integer
required
사용자가 부분 성공한 문제 수입니다.

tried
integer
required
사용자가 시도해 본 문제 수입니다.

handle*
:
string
Send API Request
curl --request GET \
  --url https://solved.ac/api/v3/user/problem_tag_stats \
  --header 'Accept: application/json'
{
  "count": 1,
  "items": [
    {
      "tag": {
        "key": "arbitrary_precision",
        "isMeta": false,
        "bojTagId": 117,
        "problemCount": 241,
        "displayNames": [
          {
            "language": "ko",
            "name": "임의 정밀도 / 큰 수 연산",
            "short": "임의 정밀도 / 큰 수 연산"
          }
        ],
        "aliases": [
          {
            "alias": "string"
          }
        ]
      },
      "total": 0,
      "solved": 0,
      "partial": 0,
      "tried": 0
    }
  ]
}