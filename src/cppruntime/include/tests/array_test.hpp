#ifndef ARRAY_TEST_HPP
#define ARRAY_TEST_HPP

static void println()
{
    std::cout << std::endl;
}

template<typename T>
static void println(T arg)
{
    std::cout << arg << std::endl;
}

template<typename T, typename...Ts>
static void println(T arg, Ts... args)
{
    std::cout << arg;
    println(args...);
}

template<typename T>
static void print(T arg)
{
    std::cout << arg;
}

template<typename T, typename... Ts>
static void print(T arg, Ts... args)
{
    std::cout << arg;
    print(args...);
}

namespace pj_tests
{
    class array_test
    {
    public:
        array_test()
        {
            std::cout << "instantiating test..." << std::endl;
        }

        void run()
        {
            pj_runtime::pj_array<int32_t>* pj_arr = new pj_runtime::pj_array<int32_t>({1, 2, 3, 4});

            int32_t i, j, k;
            for(i = 0; i < pj_arr->length; ++i)
            {
                print((*pj_arr)[i], ", ");
            }
            println();
            delete pj_arr;

            pj_runtime::pj_md_array<pj_runtime::pj_array<int32_t>*>* pj_md_arr_b =
                new pj_runtime::pj_md_array<pj_runtime::pj_array<int32_t>*>{new pj_runtime::pj_array<int32_t>{1, 2, 3, 4},
                                                                            new pj_runtime::pj_array<int32_t>{5, 6, 7, 8},
                                                                            new pj_runtime::pj_array<int32_t>{9, 10, 11, 12},
                                                                            new pj_runtime::pj_array<int32_t>{13, 14, 15, 16}};

            for(i = 0; i < pj_md_arr_b->length; ++i)
            {
                for(j = 0; j < (*pj_md_arr_b)[i]->length; ++j)
                {   
                    print((*(*pj_md_arr_b)[i])[j], ", ");
                }
            }
            println();
            delete pj_md_arr_b;

            pj_runtime::pj_md_array<pj_runtime::pj_md_array<pj_runtime::pj_array<int32_t>*>*>* pj_md_arr_c =
            new pj_runtime::pj_md_array<pj_runtime::pj_md_array<pj_runtime::pj_array<int32_t>*>*>{new pj_runtime::pj_md_array<pj_runtime::pj_array<int32_t>*>{new pj_runtime::pj_array<int32_t>{1, 2, 3, 4},
                                                                                                 new pj_runtime::pj_array<int32_t>{5, 6, 7, 8},
                                                                                                 new pj_runtime::pj_array<int32_t>{9, 10, 11, 12},
                                                                                                 new pj_runtime::pj_array<int32_t>{13, 14, 15, 16}},
                                                             new pj_runtime::pj_md_array<pj_runtime::pj_array<int32_t>*>{new pj_runtime::pj_array<int32_t>{17, 18, 19, 20},
                                                                                                 new pj_runtime::pj_array<int32_t>{21, 22, 23, 24},
                                                                                                 new pj_runtime::pj_array<int32_t>{25, 26, 27, 28},
                                                                                                 new pj_runtime::pj_array<int32_t>{29, 30, 31, 32}},
                                                             new pj_runtime::pj_md_array<pj_runtime::pj_array<int32_t>*>{new pj_runtime::pj_array<int32_t>{33, 34, 35, 36},
                                                                                                 new pj_runtime::pj_array<int32_t>{37, 38, 39, 40},
                                                                                                 new pj_runtime::pj_array<int32_t>{41, 42, 43, 44},
                                                                                                 new pj_runtime::pj_array<int32_t>{45, 46, 47, 48}},
                                                             new pj_runtime::pj_md_array<pj_runtime::pj_array<int32_t>*>{new pj_runtime::pj_array<int32_t>{49, 50, 51, 52},
                                                                                                 new pj_runtime::pj_array<int32_t>{53, 54, 55, 56},
                                                                                                 new pj_runtime::pj_array<int32_t>{57, 58, 59, 60},
                                                                                                 new pj_runtime::pj_array<int32_t>{61, 62, 63, 64}}};
            
            for(i = 0; i < pj_md_arr_c->length; ++i)
            {
                for(j = 0; j < (*pj_md_arr_c)[i]->length; ++j)
                {
                    for(k = 0; k < (*(*pj_md_arr_c)[i])[j]->length; ++k)
                    {
                        print((*(*(*pj_md_arr_c)[i])[j])[k], ", ");
                    }
                }
            }
            println();
            delete pj_md_arr_c;
        }
    };
}

#endif