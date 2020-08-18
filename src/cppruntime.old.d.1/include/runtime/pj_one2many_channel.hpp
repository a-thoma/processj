#ifndef PJ_ONE2MANY_CHANNEL_HPP
#define PJ_ONE2MANY_CHANNEL_HPP

#include <runtime/pj_channel.hpp>
#include <runtime/pj_one2one_channel.hpp>

#include <queue>

namespace pj_runtime
{
	template <typename T>
	class pj_one2many_channel : public pj_runtime::pj_one2one_channel<T>
	{
	public:
		pj_one2many_channel()
		: read_claim(nullptr)
		{
			std::cout << "pj_one2many_channel constructor called\n";
			this->type = pj_channel_types::ONE2MANY;
		}

		~pj_one2many_channel()
		{
			std::cout << "pj_one2many_channel destructor called\n";
			read_claim = nullptr;
		}

		bool claim_read(pj_process* p)
		{
			std::lock_guard<std::mutex> lock(this->mtx);
			if(!read_claim || read_claim == p)
			{
				read_claim = p;
				return true;
			}
			else
			{
				p->set_not_ready();
				read_queue.push(p);
			}

			return false;
		}

		void unclaim_read()
		{
			std::lock_guard<std::mutex> lock(this->mtx);
			if(read_queue.empty())
			{
				read_claim = nullptr;
			}
			else
			{
				pj_process* p = read_queue.front();
				read_queue.pop();
				read_claim = p;
				p->set_ready();
			}
		}

	protected:
		pj_process* read_claim = nullptr;
		std::queue<pj_process*> read_queue;
	};
}

#endif