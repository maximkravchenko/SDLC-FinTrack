import React, { useState } from 'react';
import AccountModal from './modals/AccountModal';
import UserModal from './modals/UserModal';

const Sidebar = ({ activeTab, setActiveTab, isOpen, setIsOpen, state, dispatch }) => {
    const [showAccountModal, setShowAccountModal] = useState(false);
    const [showUserModal, setShowUserModal] = useState(false);

    const menuItems = [
        { id: 'home', label: '| Главная'},
        { id: 'history', label: '| История транзакций'},
        { id: 'tags', label: '| Коллекция тегов'},
        { id: 'statistics', label: '| Статистика'},
    ];

    const sidebarClasses = `
    fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out
    ${isOpen ? 'translate-x-0' : '-translate-x-full'}
    lg:translate-x-0 lg:static lg:inset-0
  `;

    // Debugging
    console.log('Current user in Sidebar:', state.currentUser);

    return (
        <>
            {/* Hamburger Menu Button */}
            <button
                onClick={() => setIsOpen(!isOpen)}
                className="fixed top-4 left-4 z-50 lg:hidden bg-[#003464] text-white p-2 rounded-lg shadow-lg"
            >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
            </button>

            <div className={sidebarClasses}>
                <div className="flex flex-col h-full">
                    <div className="p-6 border-b border-gray-200">
                        <h1 className="text-3xl font-bold text-[#003464]">FinanceRY</h1>
                        {state.currentUser && (
                            <div className="mt-2 text-md text-gray-600">
                                Аккаунт: {state.currentUser.name}
                            </div>
                        )}
                        <button
                            onClick={() => setShowAccountModal(true)}
                            className="mt-4 w-full bg-[#003464] text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                        >
                            Переключить аккаунт
                        </button>
                    </div>

                    <nav className="flex-1 p-4">
                        <ul className="space-y-2">
                            {menuItems.map((item) => (
                                <li key={item.id}>
                                    <button
                                        onClick={() => {
                                            setActiveTab(item.id);
                                            setIsOpen(false);
                                        }}
                                        className={`w-full text-left px-4 py-3 rounded-lg transition-colors flex items-center space-x-3 ${
                                            activeTab === item.id
                                                ? 'bg-[#003464] text-white'
                                                : 'text-gray-700 hover:bg-gray-100'
                                        }`}
                                    >
                                        <span>{item.label}</span>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </nav>
                </div>
            </div>

            {isOpen && (
                <div
                    className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
                    onClick={() => setIsOpen(false)}
                />
            )}

            <AccountModal
                isOpen={showAccountModal}
                onClose={() => setShowAccountModal(false)}
                state={state}
                dispatch={dispatch}
                onOpenUserModal={() => setShowUserModal(true)}
            />

            <UserModal
                isOpen={showUserModal}
                onClose={() => setShowUserModal(false)}
                state={state}
                dispatch={dispatch}
            />
        </>
    );
};

export default Sidebar;