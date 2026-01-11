-- 1. 기존 스터디에 대한 채팅방 생성 (없는 경우에만)
INSERT INTO chat_rooms (name, type, study_id, creator_id, created_at)
SELECT CONCAT(s.name, ' 채팅방'), 'STUDY', s.id, s.creator_id, NOW()
FROM studies s
LEFT JOIN chat_rooms cr ON s.id = cr.study_id
WHERE cr.id IS NULL;

-- 2. 스터디 멤버들을 채팅방 멤버로 추가
INSERT INTO chat_room_members (room_id, user_id, joined_at)
SELECT cr.id, u.id, NOW()
FROM chat_rooms cr
JOIN users u ON cr.study_id = u.study_id
LEFT JOIN chat_room_members crm ON cr.id = crm.room_id AND u.id = crm.user_id
WHERE cr.type = 'STUDY' 
  AND u.deleted_at IS NULL
  AND crm.id IS NULL;
