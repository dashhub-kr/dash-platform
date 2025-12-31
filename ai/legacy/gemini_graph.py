"""
이 모듈은 LangGraph를 활용하여 코드 분석 워크플로우를 정의합니다.
"""

from typing import TypedDict
from langgraph.graph import StateGraph, START, END
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_core.output_parsers import PydanticOutputParser
from dotenv import load_dotenv
import os

# 공통 모듈 임포트
from schemas import StructuredResponse
from prompts import get_code_analysis_prompt_template

load_dotenv()

gemini_api_key = os.getenv("GEMINI_API_KEY")

# --- 상태(State) 정의 ---
class AgentState(TypedDict):
    """그래프 내에서 공유되는 상태 정보"""
    input_code: str
    analysis_result: StructuredResponse | None

# --- 노드(Node) 정의 ---

def analyze_code_node(state: AgentState) -> AgentState:
    """
    입력된 코드를 분석하여 StructuredResponse를 생성하는 노드입니다.
    """
    print("--- [Node] Code Analysis ---")
    code = state["input_code"]
    
    # LLM 설정
    llm = ChatGoogleGenerativeAI(
        model="gemini-2.5-flash",
        google_api_key=gemini_api_key,
        temperature=0
    )
    
    # 프롬프트 및 파서 설정 (중앙화된 로직 사용)
    parser = PydanticOutputParser(pydantic_object=StructuredResponse)
    prompt = get_code_analysis_prompt_template()
    
    chain = prompt | llm | parser
    
    try:
        print("[DEBUG] Invoking chain...")
        result = chain.invoke({"query": code})
        print("[DEBUG] Chain finished.")
        return {"analysis_result": result}
    except Exception as e:
        print(f"[ERROR] Analysis Node Failed: {e}")
        return {"analysis_result": None}

def feedback_node(state: AgentState) -> AgentState:
    """
    분석 결과를 바탕으로 추가적인 피드백을 제공하는 노드
    """
    print("--- [Node] Feedback Generation ---")
    result = state["analysis_result"]
    if result:
        print(f"Analysis Summary: {result.summary}")
    return {}

# --- 그래프(Graph) 구성 ---

workflow = StateGraph(AgentState)

# 노드 추가
workflow.add_node("analyze", analyze_code_node)
workflow.add_node("feedback", feedback_node)

# 엣지(Edge) 연결
workflow.add_edge(START, "analyze")
workflow.add_edge("analyze", "feedback")
workflow.add_edge("feedback", END)

# 그래프 컴파일
app = workflow.compile()

# --- 실행 테스트 ---
if __name__ == "__main__":
    # 테스트용 코드
    sample_code = """import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int N = sc.nextInt();
        System.out.println(N * 2);
    }
}"""
    
    print(">>> Starting Graph Execution...")
    result_state = app.invoke({"input_code": sample_code})
    
    print("\n>>> Final State Result:")
    if result_state.get("analysis_result"):
        print(result_state["analysis_result"])
    else:
        print("No result produced.")
