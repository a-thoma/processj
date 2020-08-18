#ifndef PJ_MANY2ONE_CHANNEL_HPP
#define PJ_MANY2ONE_CHANNEL_HPP

#include <runtime/pj_one2one_channel.hpp>

#include <queue>

namespace pj_runtime
{
	template <typename T>
	class pj_many2one_channel : public pj_runtime::pj_one2one_channel<T>
	{
	public:
		pj_many2one_channel()
		{
			std::cout << "pj_many2one_channel constructor called\n";
		}

		~pj_many2one_channel()
		{
			std::cout << "pj_many2one_channel destructor called\n";
		}

		bool claim_write(pj_process* p)
		{
			std::lock_guard<std::mutex> lock(this->mtx);
			if(!write_claim || write_claim == p)
			{
				write_claim = p;
				return true;
			}
			else
			{
				p->set_not_ready();
				write_queue.push(p);
			}

			return false;
		}

		void unclaim_write()
		{
			std::lock_guard<std::mutex> lock(this->mtx);
			if(write_queue.empty())
			{
				write_claim = nullptr;
			}
			else
			{
				pj_process* p = write_queue.front();
				write_queue.pop();
				write_claim = p;
				p->set_ready();
			}
		}

	protected:
		pj_process* write_claim = nullptr;
		std::queue<pj_process*> write_queue;
	};
}

#endif