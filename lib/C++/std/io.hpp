#ifndef PJ_IO_HPP
#define PJ_IO_HPP
class io
{
public:
    static void println()
    {
        std::cout << std::endl;
    }

    template<typename T>
    static void println(T v)
    {
        std::cout << v << std::endl;
    }

    // TODO: what is a byte in c++, a char...? flesh this out
    // static void println(std::byte b)
    // {
    //     std::cout << b << std::endl;
    // }

    // static void println(const short s)
    // {
    //     std::cout << s << std::endl;
    // }

    // static void println(const int i)
    // {
    //     std::cout << i << std::endl;
    // }

    // static void println(const char c)
    // {
    //     std::cout << c << std::endl;
    // }

    // static void println(const long l)
    // {
    //     std::cout << l << std::endl;
    // }

    // static void println(const float f)
    // {
    //     std::cout << f << std::endl;
    // }

    // static void println(const double d)
    // {
    //     std::cout << d << std::endl;
    // }

    // static void println(const char* s)
    // {
    //     std::cout << s << std::endl;
    // }

    // static void println(const std::string s)
    // {
    //     std::cout << s << std::endl;
    // }

    // no line feed
    
    // TODO: what is a byte in c++, a char...? flesh this out
    // static void print(std::byte b)
    // {
    //     std::cout << b;
    // }

    // static void print(short s)
    // {
    //     std::cout << s;
    // }

    // static void print(int i)
    // {
    //     std::cout << i;
    // }

    // static void print(char c)
    // {
    //     std::cout << c;
    // }

    // static void print(long l)
    // {
    //     std::cout << l;
    // }

    // static void print(float f)
    // {
    //     std::cout << f;
    // }

    // static void print(double d)
    // {
    //     std::cout << d;
    // }

    // static void print(std::string s)
    // {
    //     std::cout << s;
    // }
};
#endif