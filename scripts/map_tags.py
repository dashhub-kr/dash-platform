import json
import os

# Paths
LIST_JSON_PATH = '/Users/yongsu/ssafy/dash/list.json'
PROBLEMS_JSON_PATH = '/Users/yongsu/ssafy/final/crwaling/problem/problems.json'
PROBLEM_TAGS_JSON_PATH = '/Users/yongsu/ssafy/final/crwaling/problem/problem_tags.json'
OUTPUT_PATH = '/Users/yongsu/ssafy/dash/backend/src/main/resources/problems_with_keys.json'

def load_json(path):
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

def build_tag_map(list_data):
    tag_map = {} # Korean Name -> Tag Key
    for item in list_data['items']:
        key = item['key']
        for dn in item['displayNames']:
            if dn['language'] == 'ko':
                tag_map[dn['name']] = key
                break # Prefer primary Korean name
    return tag_map

def main():
    print("Loading data...")
    list_data = load_json(LIST_JSON_PATH)
    problems_data = load_json(PROBLEMS_JSON_PATH)
    problem_tags_data = load_json(PROBLEM_TAGS_JSON_PATH)

    print("Building tag map...")
    tag_map = build_tag_map(list_data)
    
    # Process problems
    problems_dict = {}
    for p in problems_data:
        p_id = str(p['problem_id'])
        problems_dict[p_id] = {
            "problemId": p_id,
            "title": p['title_ko'],
            "level": p['level'],
            "class": p['class'],
            "essential": p['essential'] == "1",
            "sprout": p['sprout'] == "1",
            "tags": []
        }

    print("Mapping tags...")
    for pt in problem_tags_data:
        p_id = str(pt['problem_id'])
        ko_tag = pt['tag']
        
        if p_id in problems_dict:
            if ko_tag in tag_map:
                problems_dict[p_id]['tags'].append(tag_map[ko_tag])
            else:
                pass

    # Convert to list and sort
    result = list(problems_dict.values())
    result.sort(key=lambda x: int(x['problemId']))
    
    print(f"Saving {len(result)} problems to {OUTPUT_PATH}...")
    with open(OUTPUT_PATH, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
    print("Done!")

if __name__ == '__main__':
    main()
