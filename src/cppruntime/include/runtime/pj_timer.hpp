/* TODO: inherits from concurrent libs in java, find out equivalents
 * ---
 * more specifically, find an equivalent use for:
 *     java.util.concurrent.Delayed;
 *     java.util.concurrent.TimeUnit;
 * ---
 */

#ifndef PJ_TIMER_HPP
#define PJ_TIMER_HPP

#include <runtime/pj_process.hpp>

#include <iostream>
#include <chrono>
#include <ostream>

namespace pj_runtime
{
    class pj_timer
    {
        friend class pj_timer_queue;
        
    public:
        pj_timer()
        : m_timeout(0),
          m_now(std::chrono::system_clock::now()),
          m_later(std::chrono::system_clock::time_point(std::chrono::seconds(m_timeout))),
          m_delta(m_now - m_later),
          m_expired(true),
          m_killed(false),
          m_process(static_cast<pj_process*>(nullptr))
        {
            std::cout << "pj_timer default constructor called\n";
        }

        pj_timer(pj_process* p, long timeout)
        : m_timeout(timeout),
          m_now(std::chrono::system_clock::now()),
          m_later(std::chrono::system_clock::time_point(std::chrono::seconds(m_timeout))),
          m_delta(m_now - m_later),
          m_expired(false),
          m_killed(false),
          m_process(p)
        {
            std::cout << "pj_timer long argument constructor called\n";
        }

        ~pj_timer()
        {
            std::cout << "pj_timer destructor called\n";
        }

        void start()
        {
            m_now = std::chrono::system_clock::now();
            m_later = std::chrono::system_clock::time_point(std::chrono::seconds(m_timeout));
            m_delta = m_now - m_later;
        }

        void expire()
        {
            m_expired = true;
        }

        long read()
        {
            m_delta = m_later - m_now;
            m_now = std::chrono::system_clock::now();
            return m_delta.count();
        }

        pj_process* get_process()
        {
            return (m_killed) ? static_cast<pj_process*>(0) : m_process;
        }

        friend std::ostream& operator<<(std::ostream& o, const pj_timer& t)
        {
            return o << "Process: " << t.m_process;
        }

    private:
        long m_timeout;
        std::chrono::system_clock::time_point m_now;
        std::chrono::system_clock::time_point m_later;
        std::chrono::system_clock::duration m_delta;
        bool m_expired;
        bool m_killed;
        pj_process* m_process;
    };
}

#endif