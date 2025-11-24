import os
import subprocess

ROOT_DIR = r"D:\Code\JAVA\demo\Video\data\tiny-Kinetics-400"

THUMB_DIR = r"D:\Code\JAVA\demo\Video\thumbs"

VIDEO_EXTS = [".mp4", ".avi", ".mov", ".mkv"]

os.makedirs(THUMB_DIR, exist_ok=True)


def generate_thumbnail(video_path, output_path):
    cmd = [
        "ffmpeg",
        "-i", video_path,
        "-ss", "0",
        "-frames:v", "1",
        "-q:v", "2",  # JPEG质量
        output_path,
        "-y"          # 覆盖输出文件
    ]

    print(f"[生成中] {output_path}")
    subprocess.run(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def main():
    print("🚀 开始递归扫描并生成缩略图...\n")

    for root, dirs, files in os.walk(ROOT_DIR):
        for file in files:
            ext = os.path.splitext(file)[1].lower()

            # 判断是否为视频文件
            if ext in VIDEO_EXTS:
                video_path = os.path.join(root, file)
                name_no_ext = os.path.splitext(file)[0]
                thumb_path = os.path.join(THUMB_DIR, f"{name_no_ext}.jpg")

                generate_thumbnail(video_path, thumb_path)

    print("\n✅ 全部缩略图生成完成！")


if __name__ == "__main__":
    main()
