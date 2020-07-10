#ifndef PJ_IO_HPP
#define PJ_IO_HPP
namespace io
{
    void println()
    {
        std::cout << std::endl;
    }

    void println(std::string s)
    {
        std::cout << s << std::endl;
    }
}