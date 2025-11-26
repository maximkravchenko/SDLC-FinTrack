
import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import HomePage from '../components/HomePage';
import TagsPage from '../components/TagsPage';
import TransactionHistoryPage from '../components/TransactionHistoryPage';
import StatisticsPage from '../components/StatisticsPage';
import { useFinanceData } from '../hooks/useFinanceData';

const Index = () => {
  const [activeTab, setActiveTab] = useState('home');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const { state, dispatch } = useFinanceData();

  const renderContent = () => {
    switch (activeTab) {
      case 'home':
        return <HomePage state={state} dispatch={dispatch} />;
      case 'tags':
        return <TagsPage state={state} dispatch={dispatch} />;
      case 'history':
        return <TransactionHistoryPage state={state} dispatch={dispatch} />;
      case 'statistics':
        return <StatisticsPage state={state} dispatch={dispatch} />;
      default:
        return <HomePage state={state} dispatch={dispatch} />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      <Sidebar 
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        isOpen={isSidebarOpen}
        setIsOpen={setIsSidebarOpen}
        state={state}
        dispatch={dispatch}
      />
      
      {/* Main content always centered */}
      <div className="flex-1 flex justify-center">
        <main className="p-4 lg:p-8 pt-16 lg:pt-8 w-full max-w-4xl">
          {renderContent()}
        </main>
      </div>
    </div>
  );
};

export default Index;
