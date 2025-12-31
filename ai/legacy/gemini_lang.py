"""
이 모듈은 Google Gemini AI와 LangChain을 활용하여 코드 분석 및 리팩토링 제안 서비스를 제공합니다.

주요 기능:
- LangChain의 ChatGoogleGenerativeAI를 사용하여 Gemini 모델과 통신
- schemas.py의 Pydantic 모델을 사용하여 정형화된 JSON 형태의 분석 결과 생성
- prompts.py를 사용하여 일관된 프롬프트 관리

작성자: Antigravity
수정일: 2025-12-10 (Refactored to separate modules)
"""

from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.output_parsers import PydanticOutputParser
from dotenv import load_dotenv
import os

from schemas import StructuredResponse
from prompts import get_code_analysis_prompt_template

load_dotenv()

# API 키 설정
gemini_api_key = os.getenv("GEMINI_API_KEY")
print(f"[DEBUG] API 키 확인: {'Existent' if gemini_api_key else 'Missing'}")

def generate_content_gemini(query):
    """
    LangChain을 사용하여 Gemini 모델로부터 구조화된 코드 분석 결과를 생성합니다.
    """
    print(f"[DEBUG] Gemni (LangChain) 응답 생성 시작")
    
    # 1. Output Parser 설정
    parser = PydanticOutputParser(pydantic_object=StructuredResponse)
    
    # 2. Prompt Template 설정 (prompts.py 활용)
    prompt_template = get_code_analysis_prompt_template()
    
    # 3. LLM 설정
    llm = ChatGoogleGenerativeAI(
        model="gemini-2.5-flash", 
        google_api_key=gemini_api_key,
        temperature=0 
    )
    
    # 4. Chain 연결 (LCEL)
    chain = prompt_template | llm | parser
    
    # 5. 실행
    try:
        # invoke 메서드로 체인을 실행합니다.
        result = chain.invoke({"query": query})
        print(f'---------------\n출력결과:{result}\n---------------')
        return result
    except Exception as e:
        print(f"[ERROR] LangChain 실행 중 오류 발생: {e}")
        return None

# 테스트용 코드
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