#ifndef PJ_STRING_HPP
#define PJ_STRING_HPP

namespace pj_runtime
{
	#include <cstring>

	class pj_string
	{
	public:
		pj_string()
		: m_size(static_cast<std::size_t>(0)), m_c_str(static_cast<char*>(0))
		{ }

		pj_string(const char* c_str)
		: m_size(static_cast<std::size_t>(0)), m_c_str(static_cast<char*>(0))
		{
			// grab iterator pointer
			const char* c_str_iter = c_str;

			// count the characters up to the null character

			// TODO: string literals in c++ are always null-terminated,
			// but are we guaranteed to always accept a string
			// literal?
			while(*c_str_iter++ != '\0')
			{
				++m_size;
			}
			++m_size;

			// allocate new char array
			m_c_str = new char[m_size];
			memset(m_c_str, static_cast<char>(0), m_size);

			// reset iter pointer
			c_str_iter = c_str;

			// index for array access
			std::size_t i = 0;

			// copy the actual characters
			while(*c_str_iter != '\0')
			{
				m_c_str[i++] = *c_str_iter++;
			}
		}

		pj_string(const pj_string& other)
		: m_size(static_cast<std::size_t>(0)), m_c_str(static_cast<char*>(0))
		{
			const char* other_iter = other.m_c_str;

			while(*other_iter++ != '\0')
			{
				++m_size;
			}
			++m_size;

			m_c_str = new char[m_size];
			memset(m_c_str, static_cast<char>(0), m_size);
			other_iter = other.m_c_str;
			std::size_t i = 0;

			while(*other_iter != '\0')
			{
				m_c_str[i++] = *other_iter++;
			}
		}

		~pj_string()
		{
			if(m_c_str)
			{
				delete[] m_c_str;
				m_c_str = static_cast<char*>(0);
			}
		}

		std::size_t size()
		{
			return m_size;
		}

		friend std::ostream& operator<<(std::ostream& o, const pj_string& sw)
		{
			return o << sw.m_c_str;
		}

	private:
		std::size_t m_size;
		char* m_c_str;
	};
}

#endif