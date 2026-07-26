import os
import re

directories = [
    'src/main/java/com/learning/inventory_manager/core',
    'src/main/java/com/learning/inventory_manager/gateway',
    'src/main/java/com/learning/inventory_manager/web/mapper'
]

bean_classes = []

def process_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # check if it's an interface
    if 'public interface ' in content:
        return

    # find @Component or @Service
    if not ('@Component' in content or '@Service' in content):
        return

    # remove @Component or @Service
    new_content = re.sub(r'@(Component|Service)\s*\n', '', content)
    new_content = re.sub(r'import org\.springframework\.stereotype\.(Component|Service);\s*\n', '', new_content)

    with open(filepath, 'w') as f:
        f.write(new_content)

    # extract class name
    class_match = re.search(r'public class ([A-Za-z0-9_]+)', new_content)
    if not class_match:
        return
    class_name = class_match.group(1)

    # find package
    pkg_match = re.search(r'package (.*?);', new_content)
    pkg = pkg_match.group(1) if pkg_match else ''

    # find constructor or @RequiredArgsConstructor fields
    deps = []
    if '@RequiredArgsConstructor' in new_content:
        # find private final Type name;
        fields = re.findall(r'private final ([A-Za-z0-9_<>]+) ([A-Za-z0-9_]+);', new_content)
        deps = fields
    else:
        # find constructor
        cons_match = re.search(r'public ' + class_name + r'\s*\((.*?)\)', new_content, re.DOTALL)
        if cons_match:
            params = cons_match.group(1).split(',')
            for p in params:
                p = p.strip()
                if not p: continue
                parts = p.split(' ')
                if len(parts) >= 2:
                    deps.append((parts[-2], parts[-1]))
                    
    bean_classes.append({
        'name': class_name,
        'package': pkg,
        'deps': deps
    })

for d in directories:
    for root, _, fnames in os.walk(d):
        for fname in fnames:
            if fname.endswith('.java'):
                process_file(os.path.join(root, fname))

config_path = 'src/main/java/com/learning/inventory_manager/config'
os.makedirs(config_path, exist_ok=True)

with open(os.path.join(config_path, 'AppConfig.java'), 'w') as f:
    f.write('package com.learning.inventory_manager.config;\n\n')
    
    f.write('import org.springframework.context.annotation.Bean;\n')
    f.write('import org.springframework.context.annotation.Configuration;\n\n')
    
    # write imports
    for b in bean_classes:
        f.write(f'import {b["package"]}.{b["name"]};\n')
    # also import dependencies that might be from other packages, but we'll let IDE handle it or just rely on the fact that spring injects by type
    
    f.write('\n@Configuration\n')
    f.write('public class AppConfig {\n\n')
    
    for b in bean_classes:
        args = ', '.join([f'{d[0]} {d[1]}' for d in b['deps']])
        params = ', '.join([d[1] for d in b['deps']])
        method_name = b['name'][0].lower() + b['name'][1:]
        
        f.write('    @Bean\n')
        f.write(f'    public {b["name"]} {method_name}({args}) {{\n')
        f.write(f'        return new {b["name"]}({params});\n')
        f.write('    }\n\n')
        
    f.write('}\n')

