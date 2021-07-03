cmake_minimum_required(VERSION 3.5)

# Installing dependencies and generating magura_build_info.cmake
execute_process(COMMAND magura connect --project ${CMAKE_PARENT_LIST_FILE} --output ${CMAKE_BINARY_DIR})
# Including generated magura_build_info.cmake
include(${CMAKE_BINARY_DIR}/magura_build_info.cmake)
