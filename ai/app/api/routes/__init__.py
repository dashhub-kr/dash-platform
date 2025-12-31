"""라우트 모듈"""

from .health import router as health_router
from .review import router as review_router
from .learning_path import router as learning_path_router
from .coding_style import router as coding_style_router
from .tutor import router as tutor_router
from .debug import router as debug_router
from .simulator import router as simulator_router

__all__ = [
    "health_router",
    "review_router",
    "learning_path_router",
    "coding_style_router",
    "tutor_router",
    "debug_router",
    "simulator_router",
]


