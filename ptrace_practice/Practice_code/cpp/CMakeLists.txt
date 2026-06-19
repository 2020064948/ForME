cmake_minimum_required(VERSION 3.22.1)

project("ptrace_native.cpp")

add_library(
        ptrace_native
        SHARED
        ptrace_native.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        ptrace_native
        ${log-lib}
)