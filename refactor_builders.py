import os
import re

directories = [
    'src/main/java/com/learning/inventory_manager/core/processor',
    'src/main/java/com/learning/inventory_manager/core/usecase',
    'src/main/java/com/learning/inventory_manager/web'
]

def find_java_files():
    files = []
    for d in directories:
        for root, _, fnames in os.walk(d):
            for fname in fnames:
                if fname.endswith('.java'):
                    files.append(os.path.join(root, fname))
    return files

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # We need to find block of code like:
    # SomeClass.builder()
    #     .field(value)
    #     .build()
    # and replace it with buildSomeClass(...)
    
    # Let's use a regex to find all builder patterns
    # A builder pattern usually looks like:
    # ClassName.builder()[\s\S]*?\.build\(\)
    
    # We will do this manually for the Controller, Processors, and UseCases
    # For now let's just print the matches to see if we can parse it
    
    matches = re.finditer(r'([A-Za-z0-9_]+)\s*\.builder\(\)[\s\S]*?\.build\(\)', content)
    for match in matches:
        class_name = match.group(1)
        full_match = match.group(0)
        print(f"File: {filepath}")
        print(f"Found builder for: {class_name}")

if __name__ == '__main__':
    for f in find_java_files():
        process_file(f)

