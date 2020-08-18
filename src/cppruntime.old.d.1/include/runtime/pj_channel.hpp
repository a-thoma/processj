#ifndef PJ_CHANNEL_HPP
#define PJ_CHANNEL_HPP

#include <runtime/pj_process.hpp>
#include <runtime/pj_channel_type.hpp>

#include <mutex>

namespace pj_runtime
{
	class pj_channel
	{
	public:
		pj_channel()
		: type(pj_channel_types::NONE)
		{
            std::cout << "pj_channel constructor called\n";
		}

		pj_channel(pj_channel_types t)
		: type(pj_channel_type(t))
		{
            std::cout << "pj_channel constructor called with arguments\n";
		}

		pj_channel(pj_channel_type t)
		: type(t)
		{
            std::cout << "pj_channel constructor called with arguments\n";
		}

        /* TODO: copy and move constructors/assignment operators 
         * if necessary
         */
         pj_channel(pj_channel&& other)
         {
            this->reader = other.reader;
            this->writer = other.writer;
         }

         pj_channel& operator=(pj_channel&& other)
         {
            this->reader = other.reader;
            this->writer = other.writer;
            return *this;
         }

		virtual ~pj_channel()
		{
            std::cout << "pj_channel destructor called\n";
            reader = nullptr;
            writer = nullptr;
		}

        pj_channel_type get_channel_type()
        {
        	return type;
        }

        /* NOTE: removed for non-type-generic fix */
		// virtual void        write(pj_process* p, T data)         = 0;
		// virtual T           read(pj_process* p)                  = 0;

		// virtual bool        is_ready_to_read(pj_process* p)      = 0;
		// virtual bool        is_ready_to_write(pj_process* p)     = 0;

		// virtual T           pre_read_rendezvous(pj_process* p)   = 0;
		// virtual void        post_read_rendezvous(pj_process* p)  = 0;

        /* NOTE: commented out to take impls from one2one */
		// virtual pj_process* alt_get_writer(pj_process* p)        = 0;
		// virtual pj_process* set_reader_get_writer(pj_process* p) = 0;

        pj_process* alt_get_writer(pj_process* p)
        {
            std::lock_guard<std::mutex> lock(this->mtx);
            if(!writer)
            {
                reader = p;
            }

            return writer;
        }

        pj_process* set_reader_get_writer(pj_process* p)
        {
            reader = p;
            return writer;
        }

        virtual bool claim_read(pj_process* p)
        {
        	return false;
        }

        virtual void unclaim_read(pj_process* p)
        {

        }

        virtual bool claim_write(pj_process* p)
        {
        	return false;
        }

        virtual void unclaim_write(pj_process* p)
        {

        }

        friend std::ostream& operator<<(std::ostream& o, pj_channel& c)
        {
        	o << c.type;
        	return o;
        }

	protected:
		pj_channel_type type;
		std::mutex mtx;
        pj_process* writer = nullptr;
        pj_process* reader = nullptr;
	};
}

#endif