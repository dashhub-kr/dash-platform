# DASH Design System
> **"Learning should be fun, vibrant, and clear."**

DASH의 디자인 시스템은 학습에 대한 동기를 부여하는 **생동감(Vibrant)**, 정보의 명확한 전달을 위한 **단순함(Simplicity)**, 그리고 즐거운 사용자 경험을 위한 **친근함(Playful)**을 지향합니다.

---

## 1. Color Palette
우리의 컬러 팔레트는 듀오링고의 생동감 넘치는 색채에서 영감을 받았으며, DASH만의 아이덴티티인 **Dash Blue**를 중심으로 구성됩니다.

### Core Brand Colors
가장 많이 사용되는 브랜드의 핵심 컬러입니다.

| Name | Hex | Usage | Preview |
|---|---|---|---|
| **Dash Blue** | `#3396F4` | Primary Actions, Brand Identity, Links | ![#3396F4](https://via.placeholder.com/20/3396f4/3396f4.png) |
| **White** | `#FFFFFF` | Card Backgrounds, Text on Dark | ![#FFFFFF](https://via.placeholder.com/20/ffffff/000000?text=+) |
| **Slate 50** | `#F8FAFC` | Page Backgrounds (Canvas) | ![#F8FAFC](https://via.placeholder.com/20/f8fafc/000000?text=+) |

### Functional Colors (Vibrant)
상태를 나타내거나 사용자에게 피드백을 줄 때 사용하는 선명한 컬러들입니다.

| Name | Hex | Usage | Preview |
|---|---|---|---|
| **Leaf (Green)** | `#58CC02` | Success, Correct Answer, Solved | ![#58CC02](https://via.placeholder.com/20/58cc02/58cc02.png) |
| **Beetle (Teal)** | `#2DD4BF` | Info, Neutral Positive | ![#2DD4BF](https://via.placeholder.com/20/2dd4bf/2dd4bf.png) |
| **Bee (Yellow)** | `#FFC800` | Warning, EXP, Streak | ![#FFC800](https://via.placeholder.com/20/ffc800/ffc800.png) |
| **Fox (Orange)** | `#FF9600` | High Alert, Burning Streak | ![#FF9600](https://via.placeholder.com/20/ff9600/ff9600.png) |
| **Rose (Red)** | `#FF4B4B` | Error, Danger Zones, Failed | ![#FF4B4B](https://via.placeholder.com/20/ff4b4b/ff4b4b.png) |

### Grayscale (Slate)
텍스트와 경계선에 사용되는 중립적인 색상입니다.

*   **Slate 800** (`#1E293B`): Main Headings
*   **Slate 700** (`#334155`): Body Text
*   **Slate 400** (`#94A3B8`): Subtitles, Placeholders, Icons
*   **Slate 200** (`#E2E8F0`): **Main Borders (2px)**
*   **Slate 50** (`#F8FAFC`): Backgrounds

---

## 2. Shapes & Borders
우리의 UI는 **둥글고(Round)**, **두꺼운(Bold)** 느낌을 줍니다.

### Corner Radius
*   **XL (12px)**: Small Inputs, Badges
*   **2XL (16px)**: **Default Buttons**, Cards, Modal Containers
*   **3XL (24px)**: Large Profile Cards, Featured Sections

### Borders
*   **Simplicity (Flat)**: 그림자(Box-shadow)를 최소화하고, 대신 **두꺼운 테두리(Border)**를 사용하여 구획을 나눕니다.
*   **Thickness**: 기본 **2px** (`border-2`)
*   **Color**: `border-slate-200` (기본), Active 시 `border-brand` 또는 `border-slate-800`

---

## 3. Typography
**Pretendard**를 사용하며, 가독성을 위해 자간(Tracking)을 좁게 설정합니다.

*   **Font Family**: `Pretendard`, sans-serif
*   **Weights**:
    *   **Black (900)**: Big Numbers, Main Stats
    *   **Bold (700)**: Headings, Buttons, Labels
    *   **Medium (500)**: Body Text

---

## 4. Components Guide

### Buttons
버튼은 **플랫(Flat)**하지만 시각적으로 명확해야 합니다.
(기존의 3D 입체 효과는 제거되었습니다)

*   **Style**: `w-full` (or fixed), `py-3`, `rounded-xl` or `rounded-2xl`
*   **Primary**: `bg-brand` text-white `hover:bg-blue-500`
*   **Secondary**: `bg-slate-100` text-slate-600 `hover:bg-slate-200`
*   **Outline**: `border-2 border-slate-200` bg-transparent text-slate-400

### Cards
정보를 담는 컨테이너입니다. 

*   **Style**: `bg-white`, `border-2 border-slate-200`, `rounded-3xl`
*   **Interaction**: Hover 시 `border-slate-300` 또는 배경색 변경 (`hover:bg-slate-50`)

### Inputs
사용자 입력을 받는 필드입니다.

*   **Style**: `bg-slate-50`, `border-2 border-slate-200`, `rounded-xl`
*   **Focus**: `focus:bg-white`, `focus:border-brand` (Outline None)
