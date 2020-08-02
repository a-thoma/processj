#ifndef PJ_IO_HPP
#define PJ_IO_HPP
class io
{
public:
    static void println()
    {
        std::cout << std::endl;
    }

    template <typename T>
    static void println(T arg)
    {
        std::cout << arg << std::endl;
    }

    template <typename T, typename... Args>
    static void println(T arg, Args... args)
    {
        std::cout << arg;
        println(args...);
    }

    static void print()
    {
        return;
    }

    template <typename T>
    static void print(T arg)
    {
        std::cout << arg;
    }

    template <typename T, typename... Args>
    static void print(T arg, Args... args)
    {
        std::cout << arg;
        print(args...);
    }
};
#endif