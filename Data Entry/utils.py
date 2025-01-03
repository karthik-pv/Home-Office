import os


def ensure_upload_folder_exists(folder):
    if not os.path.exists(folder):
        try:
            os.makedirs(folder)
            print(f"Created directory: {folder}")
        except Exception as e:
            print(f"Error creating directory {folder}: {str(e)}")
            raise


def createDictionary(table, columnMapper):
    column_names = [column.name for column in table.columns]
    newColumnMapper = columnMapper
    for i in columnMapper:
        if i == "fund_house":
            continue
        newColumnMapper[i] = column_names.index(columnMapper[i])
    print(newColumnMapper)
    print("Modified columnMapper:", columnMapper)
    print("End of function")
