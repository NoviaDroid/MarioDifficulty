# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 2.8

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list

# Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# The program to use to edit the cache.
CMAKE_EDIT_COMMAND = /usr/bin/ccmake

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/stathis/Libraries/bayesopt

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/stathis/Libraries/bayesopt

# Include any dependencies generated for this target.
include examples/CMakeFiles/bo_oned.dir/depend.make

# Include the progress variables for this target.
include examples/CMakeFiles/bo_oned.dir/progress.make

# Include the compile flags for this target's objects.
include examples/CMakeFiles/bo_oned.dir/flags.make

examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o: examples/CMakeFiles/bo_oned.dir/flags.make
examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o: examples/bo_oned.cpp
	$(CMAKE_COMMAND) -E cmake_progress_report /home/stathis/Libraries/bayesopt/CMakeFiles $(CMAKE_PROGRESS_1)
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Building CXX object examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o"
	cd /home/stathis/Libraries/bayesopt/examples && /usr/bin/g++   $(CXX_DEFINES) $(CXX_FLAGS) -o CMakeFiles/bo_oned.dir/bo_oned.cpp.o -c /home/stathis/Libraries/bayesopt/examples/bo_oned.cpp

examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/bo_oned.dir/bo_oned.cpp.i"
	cd /home/stathis/Libraries/bayesopt/examples && /usr/bin/g++  $(CXX_DEFINES) $(CXX_FLAGS) -E /home/stathis/Libraries/bayesopt/examples/bo_oned.cpp > CMakeFiles/bo_oned.dir/bo_oned.cpp.i

examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/bo_oned.dir/bo_oned.cpp.s"
	cd /home/stathis/Libraries/bayesopt/examples && /usr/bin/g++  $(CXX_DEFINES) $(CXX_FLAGS) -S /home/stathis/Libraries/bayesopt/examples/bo_oned.cpp -o CMakeFiles/bo_oned.dir/bo_oned.cpp.s

examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o.requires:
.PHONY : examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o.requires

examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o.provides: examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o.requires
	$(MAKE) -f examples/CMakeFiles/bo_oned.dir/build.make examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o.provides.build
.PHONY : examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o.provides

examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o.provides.build: examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o

# Object files for target bo_oned
bo_oned_OBJECTS = \
"CMakeFiles/bo_oned.dir/bo_oned.cpp.o"

# External object files for target bo_oned
bo_oned_EXTERNAL_OBJECTS =

bin/bo_oned: examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o
bin/bo_oned: lib/libbayesopt.a
bin/bo_oned: /usr/local/lib/libnlopt.a
bin/bo_oned: examples/CMakeFiles/bo_oned.dir/build.make
bin/bo_oned: examples/CMakeFiles/bo_oned.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --red --bold "Linking CXX executable ../bin/bo_oned"
	cd /home/stathis/Libraries/bayesopt/examples && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/bo_oned.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
examples/CMakeFiles/bo_oned.dir/build: bin/bo_oned
.PHONY : examples/CMakeFiles/bo_oned.dir/build

examples/CMakeFiles/bo_oned.dir/requires: examples/CMakeFiles/bo_oned.dir/bo_oned.cpp.o.requires
.PHONY : examples/CMakeFiles/bo_oned.dir/requires

examples/CMakeFiles/bo_oned.dir/clean:
	cd /home/stathis/Libraries/bayesopt/examples && $(CMAKE_COMMAND) -P CMakeFiles/bo_oned.dir/cmake_clean.cmake
.PHONY : examples/CMakeFiles/bo_oned.dir/clean

examples/CMakeFiles/bo_oned.dir/depend:
	cd /home/stathis/Libraries/bayesopt && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/stathis/Libraries/bayesopt /home/stathis/Libraries/bayesopt/examples /home/stathis/Libraries/bayesopt /home/stathis/Libraries/bayesopt/examples /home/stathis/Libraries/bayesopt/examples/CMakeFiles/bo_oned.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : examples/CMakeFiles/bo_oned.dir/depend

