#ifndef PJ_RANDOM_HPP
#define PJ_RANDOM_HPP

#include<cstdlib>

static void initRandom(long seed)
{
	std::srand(static_cast<unsigned>(seed));
}

static long longRandom()
{
	return static_cast<long>(std::rand());
}

#endif