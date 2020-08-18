#ifndef ARRAY_TEST_HPP
#define ARRAY_TEST_HPP

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
            pj_runtime::pj_array<int32_t>* pj_arr_a = new pj_runtime::pj_array<int32_t>({1, 2, 3, 4});
            pj_runtime::pj_array<pj_runtime::pj_array<int32_t>*>* pj_arr_b =
                new pj_runtime::pj_array<pj_runtime::pj_array<int32_t>*>({new pj_runtime::pj_array<int32_t>({1, 2, 3, 4}),
                                                                                new pj_runtime::pj_array<int32_t>({5, 6, 7, 8}),
                                                                                new pj_runtime::pj_array<int32_t>({9, 10, 11, 12}),
                                                                                new pj_runtime::pj_array<int32_t>({13, 14, 15, 16})
                                                                               });

            int32_t i, j;

            for(i = 0; i < pj_arr_a->length; ++i)
            {
                std::cout << (*pj_arr_a)[i] << ", ";
            }
            std::cout << std::endl;

            for(i = 0; i < pj_arr_b->length; ++i)
            {
                for(j = 0; j < (*pj_arr_b)[i]->length; ++j)
                {
                    std::cout << (*(*pj_arr_b)[i])[j] << ", ";
                }
            }
            std::cout << std::endl;

            // bad indexes are handled via exceptions -- good enough for now
            // std::cout << (*(*pj_arr_b)[10])[20] << std::endl;

            for(i = 0; i < pj_arr_b->length; ++i)
            {
                delete (*pj_arr_b)[i];
            }
            delete pj_arr_b;

            delete pj_arr_a;
        }
    };
}

#endif