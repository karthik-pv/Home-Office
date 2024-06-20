import os


def ensure_upload_folder_exists(folder):
    if not os.path.exists(folder):
        try:
            os.makedirs(folder)
            print(f"Created directory: {folder}")
        except Exception as e:
            print(f"Error creating directory {folder}: {str(e)}")
            raise
