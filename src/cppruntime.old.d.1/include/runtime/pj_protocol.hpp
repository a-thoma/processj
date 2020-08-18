#ifndef PJ_PROTOCOL_HPP
#define PJ_PROTOCOL_HPP

/* TODO: test this and make sure it works,
 * again mostly has to do with the code generated
 * ---
 * this is much better than my problems with records
 */

namespace pj_runtime
{
    template <class... Types>
	using pj_protocol = std::variant<Types...>;
}

#endif