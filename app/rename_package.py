import os
import shutil

old_pkg = "com.example"
new_pkg = "com.fauyds.quicksettings"

old_main_dir = "app/src/main/java/com/example"
new_main_dir = "app/src/main/java/com/fauyds/quicksettings"

old_test_dir = "app/src/test/java/com/example"
new_test_dir = "app/src/test/java/com/fauyds/quicksettings"

print("Starting package rename from", old_pkg, "to", new_pkg)

# 1. Replace text in all files under app/ and root gradle/manifest files
text_extensions = {".kt", ".kts", ".xml", ".pro", ".properties", ".json", ".txt"}

for root, dirs, files in os.walk("app"):
    if ".gradle" in root or "build" in root:
        continue
    for file in files:
        ext = os.path.splitext(file)[1]
        if ext in text_extensions or file in {"AndroidManifest.xml", "build.gradle.kts", "settings.gradle.kts"}:
            file_path = os.path.join(root, file)
            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()
                if old_pkg in content:
                    new_content = content.replace(old_pkg, new_pkg)
                    with open(file_path, "w", encoding="utf-8") as f:
                        f.write(new_content)
                    print(f"Updated text in: {file_path}")
            except Exception as e:
                print(f"Error processing {file_path}: {e}")

# Also check root gradle files
for root, dirs, files in os.walk("."):
    if ".gradle" in root or "build" in root or "app" in root:
        continue
    for file in files:
        if file.endswith(".kts") or file.endswith(".xml"):
            file_path = os.path.join(root, file)
            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    content = f.read()
                if old_pkg in content:
                    new_content = content.replace(old_pkg, new_pkg)
                    with open(file_path, "w", encoding="utf-8") as f:
                        f.write(new_content)
                    print(f"Updated text in root file: {file_path}")
            except Exception as e:
                pass

# 2. Move directory structures for main and test
def move_directory_contents(src, dst):
    if not os.path.exists(src):
        return
    os.makedirs(dst, exist_ok=True)
    for item in os.listdir(src):
        s = os.path.join(src, item)
        d = os.path.join(dst, item)
        if os.path.isdir(s):
            if os.path.exists(d):
                move_directory_contents(s, d)
                shutil.rmtree(s)
            else:
                shutil.move(s, d)
        else:
            if os.path.exists(d):
                os.remove(d)
            shutil.move(s, d)
    # Remove old empty parent dirs if possible
    try:
        os.rmdir(src)
    except:
        pass

print("Moving main source directory...")
move_directory_contents(old_main_dir, new_main_dir)

print("Moving test source directory...")
move_directory_contents(old_test_dir, new_test_dir)

# Clean up empty intermediate dirs like app/src/main/java/com/example -> com
def cleanup_empty_dirs(path, stop_at):
    while path and path != stop_at and os.path.exists(path):
        if not os.listdir(path):
            os.rmdir(path)
            path = os.path.dirname(path)
        else:
            break

cleanup_empty_dirs("app/src/main/java/com/example", "app/src/main/java")
cleanup_empty_dirs("app/src/test/java/com/example", "app/src/test/java")

print("Package rename complete!")
