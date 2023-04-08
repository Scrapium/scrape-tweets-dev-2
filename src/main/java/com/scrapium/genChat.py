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

print("The program returns 'EXCEPTION_ACCESS_VIOLATION (0xc0000005) at pc=0x00007ff94db31765, pid=86132, tid=86508")

print("Do you see the problem is the given code?")

print("\n\n")