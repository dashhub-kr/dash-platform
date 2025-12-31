from google import genai
from pydantic import BaseModel, Field
from typing import List
from dotenv import load_dotenv
import os

load_dotenv()

# API 키
gms_api_key = os.getenv("GMS_API_KEY")
gemini_api_key = os.getenv("GEMINI_API_KEY")

print(f"[DEBUG] API 키 첫 20자: {gms_api_key[:20] if gms_api_key else 'NONE'}")
print(f"[DEBUG] API 키 첫 20자: {gemini_api_key[:20] if gemini_api_key else 'NONE'}")


# 프롬프트 파일 읽기
try:
    with open("prompt.txt", "r", encoding="utf-8") as f:
        prompt = f.read()
except FileNotFoundError:
    print("[WARNING] prompt.txt 파일을 찾을 수 없습니다. 기본 프롬프트를 사용합니다.")
    prompt = """당신은 다른 사람이 작성한 코드를 이해하기 쉽게 코드에 대한 요약, 설명을 제공하는 AI입니다.
필수 구조는 별도 전달되는 스키마가 보장하므로, 각 필드 내용만 한국어로 간결하게 채워주세요."""

class Problem(BaseModel):
    """코드 문제에 대한 정보를 담는 Pydantic 모델."""
    description: str = Field(description="문제 설명")
    input: str = Field(description="입력 예시")
    output: str = Field(description="출력 예시")
    isGuess: bool = Field(description="문제 추론 여부")
    guessReason: str = Field(description="문제 추론 시 이유")


class Algorithm(BaseModel):
    """알고리즘 분석 정보를 담는 Pydantic 모델."""
    patterns: List[str] = Field(description="사용된 디자인 패턴 또는 주요 기법")
    intuition: str = Field(description="알고리즘의 핵심 아이디어 또는 직관")


class StructureItem(BaseModel):
    """코드 구조의 각 항목을 나타내는 Pydantic 모델."""
    name: str = Field(description="항목 이름 (예: 함수, 클래스)")
    role: str = Field(description="항목의 역할 또는 기능")


class KeyBlock(BaseModel):
    """코드의 주요 블록과 설명을 담는 Pydantic 모델."""
    code: str = Field(description="주요 코드 블록")
    explanation: str = Field(description="해당 코드 블록에 대한 설명")


class TraceExample(BaseModel):
    """코드 실행 추적 예시를 담는 Pydantic 모델."""
    hasExample: bool = Field(description="추적 예시 존재 여부")
    inputExample: str = Field(description="입력 예시")
    steps: List[str] = Field(description="각 단계별 설명")
    note: str = Field(description="특이사항 또는 추가 설명")


class Complexity(BaseModel):
    """시간 및 공간 복잡도 분석을 담는 Pydantic 모델."""
    time: str = Field(description="시간 복잡도")
    space: str = Field(description="공간 복잡도")
    explanation: str = Field(description="복잡도에 대한 설명")


class Pitfalls(BaseModel):
    """흔히 저지를 수 있는 실수와 개선 방안을 담는 Pydantic 모델."""
    items: List[str] = Field(description="주의해야 할 함정 또는 실수")
    improvements: List[str] = Field(description="개선 방안")


class Refactor(BaseModel):
    """리팩토링 제안을 담는 Pydantic 모델."""
    provided: bool = Field(description="리팩토링 코드 제공 여부")
    code: str = Field(description="리팩토링된 코드")
    explanation: str = Field(description="리팩토링에 대한 설명")


class StructuredResponse(BaseModel):
    """코드 설명 서비스의 최종 구조화된 응답 스키마."""
    summary: str
    problem: Problem
    algorithm: Algorithm
    structure: List[StructureItem]
    keyBlocks: List[KeyBlock]
    traceExample: TraceExample
    complexity: Complexity
    pitfalls: Pitfalls
    refactor: Refactor


def generate_content_gemini(query):
    print(f"[DEBUG] gemini응답 생성시작")
    client = genai.Client(api_key=gemini_api_key)
    
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=query,
        config={
            "system_instruction": prompt,
            "response_mime_type": "application/json",
            "response_json_schema": StructuredResponse.model_json_schema(),
        },
    )

    event = StructuredResponse.model_validate_json(response.text)

    print(f'---------------\n출력결과:{event}\n---------------')

    return event

text = """import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class Main {
	static HashSet<Integer> set = new HashSet<>();
	static HashSet<Integer> all = new HashSet<>();

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		for (int i = 1; i < 21; i++) {
			all.add(i);
		}
		StringBuilder sb = new StringBuilder();
		int M = sc.nextInt();
		for (int m = 0; m < M; m++) {

			String command = sc.next();
			int x = 0;
			if (!"allempty".contains(command)) {
				x = sc.nextInt();
			}

			switch (command) {
			case "add":
				set.add(x);
				break;
			case "remove":
				set.remove(x);
				break;
			case "check":
				if (set.contains(x))
					sb.append(1);
				else
					sb.append(0);
				break;
			case "toggle":
				if (set.contains(x))
					set.remove(x);
				else
					set.add(x);
				break;
			case "all":
				set.clear();
				set = all;
				break;
			case "empty":
				set.clear();
				break;

			}
		}
		System.out.println(sb);
	}
} """

if __name__ == "__main__":
    generate_content_gemini(text)