import DOMPurify from 'dompurify';

/**
 * v-html로 그릴 HTML에서 스크립트·이벤트 핸들러 등 위험 요소를 제거한다.
 * AI 응답, 문제 설명, 외부 API 응답처럼 직접 통제할 수 없는 문자열에 사용한다.
 */
export const sanitizeHtml = (html) => DOMPurify.sanitize(html ?? '');

/** 문자열을 HTML 텍스트로 안전하게 이스케이프한다. */
export const escapeHtml = (text) =>
    String(text ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
