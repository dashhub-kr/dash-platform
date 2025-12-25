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
| **Leaf (Green)** | `#58CC02` | Success, Correct Answer, Solved, **Emerald-500 equivalent** | ![#58CC02](https://via.placeholder.com/20/58cc02/58cc02.png) |
| **Beetle (Teal)** | `#2DD4BF` | Info, Neutral Positive | ![#2DD4BF](https://via.placeholder.com/20/2dd4bf/2dd4bf.png) |
| **Bee (Yellow)** | `#FFC800` | Warning, EXP, Streak, **Amber-500 equivalent** | ![#FFC800](https://via.placeholder.com/20/ffc800/ffc800.png) |
| **Fox (Orange)** | `#FF9600` | High Alert, Burning Streak, **Orange-500 equivalent** | ![#FF9600](https://via.placeholder.com/20/ff9600/ff9600.png) |
| **Rose (Red)** | `#FF4B4B` | Error, Danger Zones, Failed | ![#FF4B4B](https://via.placeholder.com/20/ff4b4b/ff4b4b.png) |

---

## 2. Shapes & Borders
우리의 UI는 **둥글고(Round)**, **두꺼운(Bold)** 느낌을 줍니다.

### Corner Radius
*   **XL (12px)**: Small Inputs, Tag Badges
*   **2XL (16px)**: **Default Buttons**, Small Cards, Inner Containers
*   **3XL (24px)**: **Main Cards**, Modals, Featured Sections

### Borders
*   **Simplicity (Flat)**: 그림자(Box-shadow)를 최소화하고, 대신 **부드러운 테두리**를 사용하여 구획을 나눕니다.
*   **Style**: `border border-slate-200` (기본)
*   **Shadow**: `shadow-sm` (기본), `shadow-md` or `shadow-xl` (Hover/Modal)

---

## 3. Typography
**Pretendard**를 사용하며, 가독성을 위해 자간(Tracking)을 좁게 설정합니다.

*   **Font Family**: `Pretendard`, sans-serif
*   **Weights**:
    *   **Black (900)**: Big Stats (Numbers)
    *   **Bold (700)**: Headings, Buttons, Labels
    *   **Medium (500)**: Body Text

---

## 4. Components Guide

### Badges (Tags)
작은 정보를 표시하는 라벨입니다.
*   **Structure**: `px-2 py-1` or `px-2.5 py-1`, `rounded-lg`
*   **Typography**: `text-xs font-bold`
*   **Variants**:
    *   **Brand**: `bg-brand-50 text-brand-700` (Hashtags)
    *   **Success**: `bg-emerald-100 text-emerald-700` (Passed, Solved)
    *   **Warning**: `bg-amber-100 text-amber-700` (Weeks)
    *   **Danger**: `bg-rose-100 text-rose-700` (Urgent, Failed)
    *   **Neutral**: `bg-slate-100 text-slate-500` (Info, Language)

### Avatars
사용자 프로필 이미지입니다.
*   **Size**: `w-8 h-8` (32px) or `w-9 h-9` (36px)
*   **Shape**: `rounded-full`
*   **Border**: `border-2 border-white` (겹쳐질 때 구분감 제공)
*   **Interaction**: `relative z-10 hover:z-20 hover:scale-110 transition-transform`

### Icons
**Lucide Vue Next** 라이브러리를 사용합니다.
*   **Style**: `stroke-width="2"` or `1.5` (Clean Look)
*   **Solid Style (Filled)**: 속이 꽉 찬 아이콘이 필요한 경우(헤더, 주요 배지 등)에는 `fill="currentColor"` 속성을 추가하여 구현합니다. 이때 `stroke-width="2.5"`로 설정하여 볼륨감을 줍니다.
*   **Usage**: 텍스트와 함께 사용 시 `gap-1` or `gap-2`로 간격 조정

### Cards
정보를 담는 컨테이너입니다. 
*   **Main Container**: `bg-white`, `rounded-3xl`, `p-6` or `p-8`, `shadow-sm`, `border border-slate-200`
*   **Interaction**: Hover 시 `translate-y` 효과나 `shadow-md` 증가

### Buttons
*   **Primary**: `bg-brand-600 hover:bg-brand-500 text-white font-bold rounded-xl shadow-md py-3`
*   **Secondary**: `bg-slate-100 hover:bg-slate-200 text-slate-600 font-bold rounded-xl py-3`
*   **Ghost/Link**: `text-slate-400 hover:text-slate-600`

---

## 5. Layout Guide
### Standard Page Layout (Unified)
모든 페이지는 일관된 사용자 경험을 위해 아래의 표준 레이아웃 구조를 따릅니다.

*   **Structure**: 2-Column Layout (Main Content + Right Sidebar)
*   **Container**: `flex justify-center p-4 md:p-8`
*   **Wrapper**: `flex gap-8 max-w-screen-xl w-full`
*   **Columns**:
    1.  **Main Content (Left)**: `flex-1 min-w-0 space-y-6`
        *   유동적으로 늘어나며, 화면 중앙을 차지합니다.
    2.  **Right Sidebar (Right)**: `w-[380px] hidden xl:flex flex-col gap-6 sticky top-8 h-[calc(100vh-4rem)]`
        *   고정된 너비(380px)를 가지며, 스크롤 시 상단에 고정됩니다.
        *   `xl` (1280px) 미만 해상도에서는 숨겨집니다.

### Code Snippet (Copy & Paste)
```html
<template>
  <div class="min-h-screen bg-white text-slate-800 pb-20">
    <!-- Main Layout Container -->
    <div class="flex justify-center p-4 md:p-8">
      <div class="flex gap-8 max-w-screen-xl w-full items-start">
        
        <!-- LEFT COLUMN: Main Content -->
        <main class="flex-1 min-w-0 space-y-6">          
          <!-- Page Header -->
          <div class="mb-8">
            <h1 class="text-2xl font-black text-slate-800 flex items-center gap-3">
              <!-- Icon -->
              Title
            </h1>
          </div>
          
          <!-- Content Sections -->
          <section>...</section>
        </main>

        <!-- RIGHT COLUMN: Sidebar -->
        <aside class="hidden xl:flex w-[380px] shrink-0 flex-col gap-6 sticky top-8 h-[calc(100vh-4rem)]">
          <!-- Sidebar Cards -->
          <div class="bg-white rounded-3xl border border-slate-200 shadow-sm p-6">
            ...
          </div>
        </aside>

      </div>
    </div>
  </div>
</template>
```

### Responsive Behavior
*   **Mobile / Tablet (< xl)**: Right Sidebar가 숨겨지고 Main Content가 화면 전체 너비를 사용합니다.
*   **Desktop (>= xl)**: Main Content와 Right Sidebar가 380px 고정 너비 사이드바와 함께 나란히 배치됩니다.

