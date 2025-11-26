import React, { useState } from 'react';
import TransactionList from './TransactionList';
import TransactionModal from './modals/TransactionModal';

// Импорт интерфейсов (предполагается, что они определены в других файлах или нужно добавить их сюда)
interface User {
    id: number;
    name: string;
    [key: string]: any;
}

interface Account {
    id: number;
    name: string;
    balance: number;
    userId: number;
    [key: string]: any;
}

interface Transaction {
    id: number;
    accountId: number;
    userId: number;
    type: 'income' | 'expense';
    amount: number;
    date: string;
    name?: string;
    description?: string;
    tags?: (number | { id: number; title?: string })[];
    [key: string]: any;
}

interface Tag {
    id: number;
    title: string;
    userId: number;
    [key: string]: any;
}

// Определение типа для dispatch
type DispatchAction = (action: { type: string; payload: any }) => void;

// Определение пропсов для TransactionHistoryPage
interface TransactionHistoryPageProps {
    state: {
        transactions: Transaction[];
        tags: Tag[];
        currentUser: User | null;
        accounts: Account[];
    };
    dispatch: DispatchAction;
}

const TransactionHistoryPage = ({ state, dispatch }: TransactionHistoryPageProps) => {
    const [filters, setFilters] = useState({
        period: 'all',
        type: 'all',
        tags: [],
    });
    const [showTransactionModal, setShowTransactionModal] = useState(false);
    const [transactionType, setTransactionType] = useState<'income' | 'expense'>('income');
    const [selectedAccountId, setSelectedAccountId] = useState<string>('');

    const { transactions, tags, currentUser, accounts } = state;

    // Filter transactions and tags for current user
    const userTransactions = transactions.filter((t) => t.userId === currentUser?.id);
    const userTags = tags.filter((tag) => tag.userId === currentUser?.id);
    const userAccounts = accounts.filter((account) => account.userId === currentUser?.id);

    const handleAddTransaction = (type: 'income' | 'expense') => {
        if (!selectedAccountId) {
            alert('Пожалуйста, выберите счет');
            return;
        }
        setTransactionType(type);
        setShowTransactionModal(true);
    };

    if (!currentUser) {
        return (
            <div className="text-center py-8">
                <p className="text-gray-500">Пожалуйста, выберите пользователя</p>
            </div>
        );
    }

    const selectedAccount = userAccounts.find((account) => account.id === parseInt(selectedAccountId));

    return (
        <div className="max-w-6xl mx-auto space-y-6">
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-800 mb-2">История транзакций</h1>
                <p className="text-sm text-gray-600">Все транзакции для пользователя: {currentUser.name}</p>
            </div>

            {/* Transaction Buttons and Account Selector */}
            <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Выберите счет</label>
                <select
                    value={selectedAccountId}
                    onChange={(e) => setSelectedAccountId(e.target.value)}
                    className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
                >
                    <option value="">Выберите счет</option>
                    {userAccounts.map((account) => (
                        <option key={account.id} value={account.id}>
                            {account.name}
                        </option>
                    ))}
                </select>
            </div>
            <div className="flex flex-col sm:flex-row items-center gap-4 mb-6">
                <div className="flex space-x-4">
                    <button
                        onClick={() => handleAddTransaction('income')}
                        className="bg-green-600 text-white px-8 py-3 rounded-lg hover:bg-green-700 transition-colors flex items-center space-x-2"
                    >
                        <span className="text-xl">+</span>
                        <span>Доход</span>
                    </button>
                    <button
                        onClick={() => handleAddTransaction('expense')}
                        className="bg-red-600 text-white px-8 py-3 rounded-lg hover:bg-red-700 transition-colors flex items-center space-x-2"
                    >
                        <span className="text-xl">−</span>
                        <span>Расход</span>
                    </button>
                </div>
            </div>

            <TransactionList
                transactions={userTransactions}
                tags={userTags}
                filters={filters}
                setFilters={setFilters}
                state={state}
                dispatch={dispatch}
                title="Все транзакции"
            />

            {showTransactionModal && (
                <TransactionModal
                    isOpen={showTransactionModal}
                    onClose={() => setShowTransactionModal(false)}
                    type={transactionType}
                    account={selectedAccount}
                    transaction={null}
                    state={state}
                    dispatch={dispatch}
                />
            )}
        </div>
    );
};

export default TransactionHistoryPage;