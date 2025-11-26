import React, { useState } from 'react';
import EditAccountModal from './modals/EditAccountModal';
import { api } from '@/services/api';

const AccountCard = ({ account, transactions, state, dispatch }) => {
  const [showEditModal, setShowEditModal] = useState(false);

  const accountTransactions = transactions.filter((t) => t.accountId === account.id);
  const income = accountTransactions
      .filter((t) => t.type === 'income')
      .reduce((sum, t) => sum + t.amount, 0);
  const expenses = Math.abs(
      accountTransactions
          .filter((t) => t.type === 'expense')
          .reduce((sum, t) => sum + t.amount, 0)
  );

  const handleDelete = async () => {
    if (window.confirm('Вы уверены, что хотите удалить этот аккаунт?')) {
      try {
        await api.accounts.delete(account.id);
        dispatch({ type: 'DELETE_ACCOUNT', payload: account.id });
      } catch (error) {
        console.error('Failed to delete account:', error);
        alert('Ошибка при удалении счета');
      }
    }
  };

  return (
      <>
        <div className="bg-[#003464] text-white rounded-lg p-4 shadow-lg relative overflow-hidden h-[200px]">
          <div className="h-full flex flex-col justify-between relative z-10">
            <div>
              <div className="flex justify-between items-start mb-2">
                <h3 className="text-lg font-semibold truncate">{account.name}</h3>
                <div className="flex space-x-2">
                  <button
                      onClick={() => setShowEditModal(true)}
                      className="text-white/80 hover:text-white transition-colors"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                      />
                    </svg>
                  </button>
                  <button
                      onClick={handleDelete}
                      className="text-white/80 hover:text-white transition-colors"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                      />
                    </svg>
                  </button>
                </div>
              </div>
              <div className="mb-2">
                <p className="text-white/80 text-xs">Баланс</p>
                <p className="text-xl font-bold">
                  {account.balance.toLocaleString('be-BY', { minimumFractionDigits: 2 })} BYN
                </p>
              </div>
            </div>
            <div className="flex justify-between text-xs">
              <div>
                <p className="text-white/60">Доходы</p>
                <p className="text-green-400 font-semibold">
                  +{income.toLocaleString('be-BY', { minimumFractionDigits: 2 })} BYN
                </p>
              </div>
              <div className="text-right">
                <p className="text-white/60">Расходы</p>
                <p className="text-red-400 font-semibold">
                  -{expenses.toLocaleString('be-BY', { minimumFractionDigits: 2 })} BYN
                </p>
              </div>
            </div>
          </div>
        </div>

        <EditAccountModal
            isOpen={showEditModal}
            onClose={() => setShowEditModal(false)}
            account={account}
            state={state}
            dispatch={dispatch}
        />
      </>
  );
};

export default AccountCard;