#ifndef PJ_ARRAY_HPP
#define PJ_ARRAY_HPP

#include <iostream>
#include <sstream>
#include <array>
#include <memory>
#include <cstring>

namespace pj_runtime
{
    template<typename T>
    class pj_array
    {
    public:
        int32_t length;

        pj_array()
        {
            m_array = nullptr;
        }

        pj_array(std::initializer_list<T> values)
        :length(values.size())
        {
            m_array = new T[length];

            std::copy(values.begin(), values.end(), m_array);
        }

        ~pj_array()
        {
            if(m_array)
            {
                delete[] m_array;
                m_array = nullptr;
            }
        }

        T& operator[](int32_t idx)
        {
            if(idx > length)
            {
                std::ostringstream message;
                message << "Invalid Argument: index "
                        << idx << " is out of bounds (length is "
                        << length << ")."
                        << std::endl;
                throw std::invalid_argument(message.str());
            }

            return m_array[idx];
        }

        const T& operator[](int32_t idx) const
        {
            if(idx > length)
            {
                std::ostringstream message;
                message << "Invalid Argument: index "
                        << idx << " is out of bounds (length is "
                        << length << ")."
                        << std::endl;
                throw std::invalid_argument(message.str());
            }

            return m_array[idx];
        }


    private:
        T* m_array;
    };
}

#endif