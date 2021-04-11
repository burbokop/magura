cmake_minimum_required(VERSION 3.5)
execute_process(COMMAND magura connect --project ${CMAKE_PARENT_LIST_FILE} --output ${CMAKE_BINARY_DIR})
include(${CMAKE_BINARY_DIR}/magura_build_info.cmake)
