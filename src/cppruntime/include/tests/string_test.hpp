#ifndef PJ_STRING_TEST_HPP
#define PJ_STRING_TEST_HPP

namespace pj_tests
{
	class string_test
	{
	public:
		string_test()
		{
			std::cout << "instantiating test..." << std::endl;
		}

		~string_test() = default;

		void run()
		{
			std::cout << "should call pj_string constructor for const char* argument" << std::endl;
			pj_runtime::pj_string str("Hello, world!");
			std::cout << str << std::endl;

			std::cout << "should call pj_string constructor for const char* argument" << std::endl;
			pj_runtime::pj_string str2 = "Goodbye, world!";
			std::cout << str2 << std::endl;

			std::cout << "should call copy constructor for pj_string& arg" << std::endl;
			pj_runtime::pj_string str3 = str;
			std::cout << str3 << std::endl;

			std::cout << "should call pj_string constructor for const char* argument" << std::endl;
			pj_runtime::pj_string str4("");
			std::cout << "should be empty: " << str4 << std::endl;

			std::cout << "should call pj_string constructor for const char* argument" << std::endl;
			std::cout << pj_runtime::pj_string("I'm finished here!") << std::endl;
		}
	};
}

#endif