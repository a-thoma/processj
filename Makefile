# begin makefile -- remember make -j(max threads running + 2)
RUNTIMEDIR := src/cppruntime/

RUNTIMEFLAGS := -L$(RUNTIMEDIR)lib -lprocessj

CPPFLAGS := -Wall -g -I$(RUNTIMEDIR)include -pthread -std=c++17

BUILDFLAGS := -Wall -g -I$(RUNTIMEDIR)include -Ilib/C++ -pthread -std=c++17

OFLAGS := $(CPPFLAGS) -c -fPIC

all: directoryexists $(RUNTIMEDIR)lib/libprocessj.a

tests: directoryexists $(RUNTIMEDIR)lib/libprocessj.a testbed

build: directoryexists $(RUNTIMEDIR)lib/libprocessj.a $(NAME)

$(NAME): builds/$(NAME).cpp
	g++ $(BUILDFLAGS) -o $@ $< $(RUNTIMEFLAGS)

testbed: $(RUNTIMEDIR)src/tests/pj_process_tests.cpp $(RUNTIMEDIR)include/tests/pj_process_tests.hpp
	g++ $(CPPFLAGS) -o $@ $< $(RUNTIMEFLAGS)

directoryexists:
	if [ ! -d "./$(RUNTIMEDIR)lib" ]; then mkdir -p $(RUNTIMEDIR)lib; fi

$(RUNTIMEDIR)lib/libprocessj.a: $(RUNTIMEDIR)lib/pj_runtime.o $(RUNTIMEDIR)lib/pj_utilities.o
	ar cvq $@ $^
	ranlib $@
	rm -f $^

$(RUNTIMEDIR)lib/pj_runtime.o: $(RUNTIMEDIR)src/runtime/pj_runtime.cpp $(RUNTIMEDIR)include/runtime/pj_runtime.hpp
	g++ $(OFLAGS) -o $@ $<

$(RUNTIMEDIR)lib/pj_utilities.o: $(RUNTIMEDIR)src/utilities/pj_utilities.cpp $(RUNTIMEDIR)include/utilities/pj_utilities.hpp
	g++ $(OFLAGS) -o $@ $<

# NOTE: this will be changed to the actual name of the runtime binary
# when the runtime is fully implemented, and is essentially how
# programs will be built using this system
# pj_runtime: src/runtime/pj_runtime.cpp include/runtime/pj_runtime.hpp
# 	g++ $(CPPFLAGS) -o $@ $< $(RUNTIMEFLAGS)

clean:
	rm -rf $(RUNTIMEDIR)lib/libprocessj.a pj_runtime testbed $(NAME)