"""듀얼 모델 코드 설명 서비스를 위한 FastAPI 진입점."""

from fastapi import FastAPI
from pydantic import BaseModel, Field
from typing import Optional, List
from google import genai
from dotenv import load_dotenv
import os

from gemini import generate_content_gemini, StructuredResponse

load_dotenv()
gemini_api_key = os.getenv("GEMINI_API_KEY")

app = FastAPI(
    title="Dash AI Server",
    description="알고리즘 코드 분석 AI 서버",
    version="1.0.0"
)


class CodeReviewRequest(BaseModel):
    """코드 리뷰 요청 스키마"""
    code: str
    language: Optional[str] = "java"
    problemNumber: Optional[str] = None


class UserContext(BaseModel):
    """사용자 컨텍스트"""
    weakTags: List[str] = []
    solvedCount: int = 0
    tier: int = 0
    tierName: str = "Unrated"


class HintRequest(BaseModel):
    """힌트 요청 스키마"""
    problemNumber: str
    problemTitle: Optional[str] = None
    level: int = Field(ge=1, le=3, default=1)  # 1: 유형, 2: 접근법, 3: 상세
    userContext: Optional[UserContext] = None


class HintResponse(BaseModel):
    """힌트 응답 스키마"""
    level: int
    hint: str
    encouragement: str
    relatedConcepts: List[str]
    nextStepSuggestion: str


@app.get("/")
def read_root():
    """상태 확인 엔드포인트."""
    return {"status": "ok", "service": "Dash AI Server"}


@app.post("/review", response_model=StructuredResponse)
async def review_code(request: CodeReviewRequest):
    """코드 분석 엔드포인트 - Dash Backend에서 호출"""
    result = generate_content_gemini(request.code)
    return result


@app.post("/hint", response_model=HintResponse)
async def generate_hint(request: HintRequest):
    """힌트 생성 엔드포인트 - 레벨별 맞춤 힌트 제공"""
    
    # 레벨별 프롬프트 구성
    level_prompts = {
        1: "핵심 알고리즘 유형만 간단히 알려주세요. 구체적인 풀이는 언급하지 마세요.",
        2: "구체적인 접근법과 전략을 설명해주세요. 하지만 코드나 수도코드는 제공하지 마세요.",
        3: "상세한 풀이 가이드와 수도코드를 제공해주세요."
    }
    
    weak_tags_str = ", ".join(request.userContext.weakTags) if request.userContext and request.userContext.weakTags else "없음"
    tier_info = f"{request.userContext.tierName} (Tier {request.userContext.tier})" if request.userContext else "정보 없음"
    
    prompt = f"""당신은 알고리즘 튜터입니다. 학생에게 문제에 대한 힌트를 제공합니다.

문제 번호: {request.problemNumber}
문제 제목: {request.problemTitle or "알 수 없음"}
힌트 레벨: {request.level}

학생 정보:
- 티어: {tier_info}
- 푼 문제 수: {request.userContext.solvedCount if request.userContext else 0}
- 약점 태그: {weak_tags_str}

요청: {level_prompts[request.level]}

응답 형식 (JSON):
{{
    "level": {request.level},
    "hint": "힌트 내용",
    "encouragement": "격려 메시지",
    "relatedConcepts": ["관련 개념1", "관련 개념2"],
    "nextStepSuggestion": "추가 학습 추천"
}}
"""
    
    client = genai.Client(api_key=gemini_api_key)
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_json_schema": HintResponse.model_json_schema(),
        },
    )
    
    result = HintResponse.model_validate_json(response.text)
    return result


# ---- Learning Path Endpoint ----

class TagInfo(BaseModel):
    """태그 정보"""
    tagKey: str
    solved: int
    total: int


class ClassInfo(BaseModel):
    """클래스 정보"""
    classNumber: int
    essentialSolved: int
    essentials: int
    completionRate: float


class LearningPathRequest(BaseModel):
    """학습 경로 요청 스키마"""
    currentLevel: str
    nextGoal: str
    weaknessTags: List[TagInfo] = []
    strengthTags: List[TagInfo] = []
    classProgress: List[ClassInfo] = []
    solvedCount: int = 0
    balanceType: Optional[str] = "BALANCED"
    growthTrend: Optional[str] = "STABLE"


class LearningPhase(BaseModel):
    """학습 단계"""
    priority: int
    title: str
    description: str
    estimatedTime: str
    actionItems: List[str]


class LearningPathResponse(BaseModel):
    """학습 경로 응답 스키마"""
    overallAssessment: str
    keyStrength: str
    primaryWeakness: str
    personalizedAdvice: str
    phases: List[LearningPhase]
    motivationalMessage: str


@app.post("/learning-path", response_model=LearningPathResponse)
async def generate_learning_path(request: LearningPathRequest):
    """AI 개인화 학습 경로 생성 엔드포인트"""
    
    # 약점/강점 정보 포맷
    weakness_str = "\n".join([f"  - {t.tagKey}: {t.solved}/{t.total}문제" for t in request.weaknessTags]) or "  없음"
    strength_str = "\n".join([f"  - {t.tagKey}: {t.solved}/{t.total}문제" for t in request.strengthTags]) or "  없음"
    class_str = "\n".join([f"  - Class {c.classNumber}: {c.essentialSolved}/{c.essentials} ({c.completionRate:.0f}%)" 
                          for c in request.classProgress]) or "  없음"
    
    prompt = f"""당신은 알고리즘 학습 코치입니다. 사용자의 분석 데이터를 기반으로 개인화된 학습 경로를 제안해주세요.

## 사용자 현황
- 현재 레벨: {request.currentLevel}
- 다음 목표: {request.nextGoal}
- 총 푼 문제: {request.solvedCount}문제
- 학습 유형: {request.balanceType}
- 성장 추세: {request.growthTrend}

## 약점 태그
{weakness_str}

## 강점 태그
{strength_str}

## 클래스 진행도
{class_str}

위 정보를 분석하여 다음 형식의 JSON으로 개인화된 학습 경로를 제안해주세요.
각 단계는 구체적이고 실천 가능해야 합니다.

{{
    "overallAssessment": "현재 상태에 대한 종합 평가",
    "keyStrength": "가장 큰 강점",
    "primaryWeakness": "가장 시급한 약점",
    "personalizedAdvice": "개인화된 조언 (2-3문장)",
    "phases": [
        {{
            "priority": 1,
            "title": "첫 번째 단계 제목",
            "description": "상세 설명",
            "estimatedTime": "예상 소요 시간 (예: 1-2주)",
            "actionItems": ["구체적 실천 항목 1", "구체적 실천 항목 2"]
        }}
    ],
    "motivationalMessage": "동기부여 메시지"
}}
"""
    
    client = genai.Client(api_key=gemini_api_key)
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_json_schema": LearningPathResponse.model_json_schema(),
        },
    )
    
    result = LearningPathResponse.model_validate_json(response.text)
    return result


# ---- Coding Style Analysis Endpoint ----

class CodeSample(BaseModel):
    """코드 샘플"""
    code: str
    language: str = "java"
    problemNumber: Optional[str] = None
    runtimeMs: int = 0
    memoryKb: int = 0


class UserStats(BaseModel):
    """사용자 통계"""
    totalSolved: int = 0
    avgRuntime: float = 0
    avgMemory: float = 0
    preferredTags: List[str] = []
    tier: str = "Unrated"


class CodingStyleRequest(BaseModel):
    """코딩 스타일 분석 요청"""
    codeSamples: List[CodeSample]
    userStats: Optional[UserStats] = None


class StyleAxis(BaseModel):
    """스타일 축"""
    axis: str
    result: str
    score: int
    leftLabel: str
    rightLabel: str
    description: str


class CodingStyleResponse(BaseModel):
    """코딩 스타일 분석 응답 (MBTI 스타일)"""
    mbtiCode: str
    nickname: str
    summary: str
    axes: List[StyleAxis]
    strengths: List[str]
    improvements: List[str]
    compatibleStyles: str
    advice: str


@app.post("/coding-style", response_model=CodingStyleResponse)
async def analyze_coding_style(request: CodingStyleRequest):
    """코딩 스타일 분석 엔드포인트 (MBTI 스타일)"""
    
    # 코드 샘플 요약
    code_samples_str = "\n---\n".join([
        f"```{s.language}\n{s.code[:500]}{'...' if len(s.code) > 500 else ''}\n```\n(런타임: {s.runtimeMs}ms, 메모리: {s.memoryKb}KB)"
        for s in request.codeSamples[:5]
    ])
    
    stats = request.userStats or UserStats()
    
    prompt = f"""당신은 코딩 스타일 분석 전문가입니다. 사용자의 코드를 분석하여 MBTI처럼 4가지 축으로 코딩 스타일을 분류해주세요.

## 분석할 코드 샘플들
{code_samples_str}

## 사용자 통계
- 총 푼 문제: {stats.totalSolved}문제
- 평균 런타임: {stats.avgRuntime:.0f}ms
- 평균 메모리: {stats.avgMemory:.0f}KB
- 선호 태그: {', '.join(stats.preferredTags) or '없음'}
- 티어: {stats.tier}

## 4가지 축 설명
1. **E/I 축 (External/Internal)**: 외향적 코딩(라이브러리/API 적극 사용) vs 내향적 코딩(직접 구현 선호)
2. **S/N 축 (Systematic/Intuitive)**: 체계적 코딩(꼼꼼한 예외처리) vs 직관적 코딩(핵심 로직 집중)
3. **T/F 축 (Time/Flow)**: 시간 최적화 우선 vs 가독성/흐름 우선
4. **J/P 축 (Judging/Perceiving)**: 계획적 코딩(구조화된 접근) vs 유연한 코딩(실험적 접근)

위 코드들을 분석하여 다음 JSON 형식으로 응답해주세요:

{{
    "mbtiCode": "INTP",
    "nickname": "논리적 설계자",
    "summary": "종합 설명 (2-3문장)",
    "axes": [
        {{
            "axis": "E/I",
            "result": "I",
            "score": 65,
            "leftLabel": "외향적 코딩",
            "rightLabel": "내향적 코딩",
            "description": "축별 상세 설명"
        }}
    ],
    "strengths": ["강점1", "강점2", "강점3"],
    "improvements": ["개선점1", "개선점2"],
    "compatibleStyles": "잘 맞는 스타일 (예: ENTJ)",
    "advice": "조언"
}}
"""
    
    client = genai.Client(api_key=gemini_api_key)
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_json_schema": CodingStyleResponse.model_json_schema(),
        },
    )
    
    result = CodingStyleResponse.model_validate_json(response.text)
    return result


# ---- Interactive Tutor Endpoint ----

class ChatMessage(BaseModel):
    """대화 메시지"""
    role: str  # "user" or "assistant"
    content: str


class TutorUserContext(BaseModel):
    """튜터 사용자 컨텍스트"""
    tier: str = "Unrated"
    solvedCount: int = 0
    recentTags: List[str] = []


class TutorChatRequest(BaseModel):
    """튜터 대화 요청"""
    message: str
    problemNumber: Optional[str] = None
    code: Optional[str] = None
    history: List[ChatMessage] = []
    context: Optional[TutorUserContext] = None


class TutorChatResponse(BaseModel):
    """튜터 대화 응답"""
    reply: str
    teachingStyle: str
    followUpQuestions: List[str]
    conceptExplanation: Optional[str] = None
    encouragement: str


@app.post("/tutor/chat", response_model=TutorChatResponse)
async def tutor_chat(request: TutorChatRequest):
    """대화형 튜터 채팅 엔드포인트 (소크라테스 교수법)"""
    
    # 대화 히스토리 포맷
    history_str = "\n".join([
        f"{'👤 학생' if m.role == 'user' else '🤖 튜터'}: {m.content}"
        for m in request.history[-10:]  # 최근 10개만
    ]) if request.history else "없음"
    
    ctx = request.context or TutorUserContext()
    
    code_section = ""
    if request.code:
        code_section = f"\n## 관련 코드\n```\n{request.code[:1000]}{'...' if len(request.code) > 1000 else ''}\n```"
    
    problem_section = f"\n## 관련 문제: #{request.problemNumber}" if request.problemNumber else ""
    
    prompt = f"""당신은 친절하고 격려하는 알고리즘 튜터입니다. 소크라테스 교수법을 사용하여 학생이 스스로 답을 찾도록 유도하세요.

## 대화 히스토리
{history_str}

## 학생의 현재 메시지
{request.message}
{problem_section}
{code_section}

## 학생 정보
- 티어: {ctx.tier}
- 푼 문제 수: {ctx.solvedCount}개
- 최근 관심 태그: {', '.join(ctx.recentTags) or '없음'}

## 교수법 가이드라인
1. **소크라테스식**: 직접적인 답 대신 질문을 통해 생각을 유도
2. **격려**: 어려움을 겪어도 포기하지 않도록 격려
3. **단계적 접근**: 복잡한 개념을 작은 단계로 나누어 설명
4. **맞춤형**: 학생의 티어와 경험에 맞는 수준으로 대화

응답 형식 (JSON):
{{
    "reply": "튜터의 응답 (한국어, 200자 이내)",
    "teachingStyle": "socratic|direct|hint",
    "followUpQuestions": ["후속 질문 1", "후속 질문 2"],
    "conceptExplanation": "관련 개념 설명 (필요시)",
    "encouragement": "격려 메시지"
}}
"""
    
    client = genai.Client(api_key=gemini_api_key)
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt,
        config={
            "response_mime_type": "application/json",
            "response_json_schema": TutorChatResponse.model_json_schema(),
        },
    )
    
    result = TutorChatResponse.model_validate_json(response.text)
    return result


@app.post("/generate")
async def generate_endpoint(query: str):
    """[Legacy] Gemini 분석기를 실행하고 구조화된 결과를 반환합니다."""
    result = generate_content_gemini(query)
    return {"result": result}
