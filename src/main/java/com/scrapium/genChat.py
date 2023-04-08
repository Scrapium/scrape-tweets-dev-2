import os

extension = ".java" # Replace with the extension you want to search for

# Get the current directory path
current_dir = os.getcwd()

# Use list comprehension to get all the files with the extension in the current directory
files_with_extension = [file for file in os.listdir(current_dir) if file.endswith(extension)]

# Print the list of files
for file in files_with_extension:
    f = open(file, 'r')

    print("\n\n\n#### " + file + "\n")
    for line in f:
        print(line.replace("\n", ""))

    print("\n")