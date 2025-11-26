import TransactionModal from "@/components/modals/TransactionModal.tsx";
import { useState } from "react";

const TransactionList = ({ transactions, tags, filters, setFilters, state, dispatch, title = "История транзакций" }) => {
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [showTransactionModal, setShowTransactionModal] = useState(false);

  const parseDate = (dateStr) => {
    if (!dateStr) return new Date();
    if (dateStr.includes('.')) {
      const [day, month, year] = dateStr.split('.');
      return new Date(`${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`);
    }
    return new Date(dateStr);
  };

  const normalizeTagIds = (tagData) => {
    if (!tagData || !Array.isArray(tagData)) return [];
    return tagData.map((tag) => (typeof tag === 'object' && tag.id ? tag.id : tag)).filter(Boolean);
  };

  const getTagNames = (tagData) => {
    const tagIds = normalizeTagIds(tagData);
    return tagIds
        .map((tagId) => {
          const tag = tags.find((t) => t.id === tagId);
          return tag ? tag.title : '';
        })
        .filter(Boolean)
        .slice(0, 2);
  };

  const filteredTransactions = transactions.filter((transaction) => {
    // Фильтр по периоду
    if (filters.period === 'month') {
      const now = new Date();
      const transactionDate = parseDate(transaction.date);
      if (transactionDate.getMonth() !== now.getMonth() || transactionDate.getFullYear() !== now.getFullYear()) {
        return false;
      }
    }

    // Фильтр по типу
    if (filters.type !== 'all' && transaction.type !== filters.type) {
      return false;
    }

    // Фильтр по тегам
    if (filters.tags.length > 0) {
      const transactionTagIds = normalizeTagIds(transaction.tags);
      const hasAllSelectedTags = filters.tags.every((tagId) => transactionTagIds.includes(tagId));
      if (!hasAllSelectedTags) return false;
    }

    return true;
  });

  console.log('Transactions in TransactionList:', transactions);
  console.log('Filtered transactions:', filteredTransactions);

  const handleTransactionClick = (transaction) => {
    setSelectedTransaction(transaction);
    setShowTransactionModal(true);
  };

  const getTransactionAccount = (transaction) => {
    return state.accounts.find((account) => account.id === transaction.accountId);
  };

  return (
      <div className="bg-white rounded-lg shadow-sm border">
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-xl font-bold text-gray-800 mb-4">{title}</h2>
          <div className="flex flex-wrap gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Период</label>
              <select
                  value={filters.period}
                  onChange={(e) => setFilters({ ...filters, period: e.target.value })}
                  className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
              >
                <option value="all">Все время</option>
                <option value="month">Этот месяц</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Тип</label>
              <select
                  value={filters.type}
                  onChange={(e) => setFilters({ ...filters, type: e.target.value })}
                  className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
              >
                <option value="all">Все</option>
                <option value="income">Только доходы</option>
                <option value="expense">Только расходы</option>
              </select>
            </div>
            <div className="flex-1">
              <label className="block text-sm font-medium text-gray-700 mb-1">Теги</label>
              <div className="flex flex-wrap gap-2 max-h-20 overflow-y-auto items-center">
                {tags.map((tag) => (
                    <button
                        key={tag.id}
                        onClick={() => {
                          const newTags = filters.tags.includes(tag.id)
                              ? filters.tags.filter((id) => id !== tag.id)
                              : [...filters.tags, tag.id];
                          setFilters({ ...filters, tags: newTags });
                        }}
                        className={`px-3 py-1 rounded-lg text-m transition-colors my-1 ${
                            filters.tags.includes(tag.id)
                                ? 'bg-[#003464] text-white'
                                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                    >
                      {tag.title}
                    </button>
                ))}
              </div>
            </div>
          </div>
        </div>
        <div className="divide-y divide-gray-200">
          {filteredTransactions.length === 0 ? (
              <div className="p-6 text-center text-gray-500">Транзакции не найдены</div>
          ) : (
              filteredTransactions.map((transaction) => (
                  <div
                      key={transaction.id}
                      onClick={() => handleTransactionClick(transaction)}
                      className="p-4 hover:bg-gray-50 cursor-pointer transition-colors"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-4">
                          <div className="flex-1">
                            <p className="font-medium text-gray-900 inline">{transaction.name || 'Без названия'}</p>
                            <p className="text-sm text-gray-500">{transaction.description}</p>
                            <p className="text-sm text-gray-500">
                              {parseDate(transaction.date).toLocaleDateString('ru-RU', {
                                day: '2-digit',
                                month: '2-digit',
                                year: 'numeric',
                              })}
                            </p>
                            {normalizeTagIds(transaction.tags).length > 0 && (
                                <div className="flex flex-wrap gap-1 mt-1">
                                  {getTagNames(transaction.tags).map((tagName, index) => (
                                      <span key={index} className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded">
                                        {tagName}
                                      </span>
                                  ))}
                                  {normalizeTagIds(transaction.tags).length > 2 && (
                                      <span className="inline-block px-2 py-1 text-xs bg-gray-100 text-gray-600 rounded">...</span>
                                  )}
                                </div>
                            )}
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <p
                            className={`text-lg font-semibold ${
                                transaction.type === 'income' ? 'text-green-600' : 'text-red-600'
                            }`}
                        >
                          {transaction.type === 'income' ? '+' : '-'}
                          {Math.abs(transaction.amount).toLocaleString('be-BY', { minimumFractionDigits: 2 })} BYN
                        </p>
                      </div>
                    </div>
                  </div>
              ))
          )}
        </div>
        {selectedTransaction && (
            <TransactionModal
                isOpen={showTransactionModal}
                onClose={() => {
                  setShowTransactionModal(false);
                  setSelectedTransaction(null);
                }}
                type={selectedTransaction.type} // Теперь type всегда 'income'/'expense'
                account={getTransactionAccount(selectedTransaction)}
                transaction={selectedTransaction}
                state={state}
                dispatch={dispatch}
            />
        )}
      </div>
  );
};

export default TransactionList;