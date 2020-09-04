#ifndef PJ_RTTI_HPP
#define PJ_RTTI_HPP

#include <type_traits>

namespace pj_utilities
{
    template <typename T, typename U>
    static bool instance_of(U u)
    {
        if(std::is_class<U>::value)
        {
            return std::is_base_of<T, U>::value;
        }

        return std::is_same<T, U>::value;
    }
}

#endif