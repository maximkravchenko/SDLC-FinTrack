import { useState, useEffect, useReducer } from 'react';
import { api } from '../services/api';

const initialState = {
  users: [],
  currentUser: null,
  accounts: [],
  currentAccount: null,
  transactions: [],
  tags: [],
  loading: false,
  error: null,
};

const financeReducer = (state, action) => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'SET_ERROR':
      return { ...state, error: action.payload, loading: false };
    case 'SET_USERS':
      return { ...state, users: action.payload };
    case 'ADD_USER':
      return { ...state, users: [...state.users, action.payload] };
    case 'UPDATE_USER':
      return {
        ...state,
        users: state.users.map((u) =>
            u.id === action.payload.id ? action.payload : u
        ),
        currentUser:
            state.currentUser?.id === action.payload.id
                ? action.payload
                : state.currentUser,
      };
    case 'DELETE_USER':
      return {
        ...state,
        users: state.users.filter((u) => u.id !== action.payload),
        currentUser:
            state.currentUser?.id === action.payload ? null : state.currentUser,
      };
    case 'SET_CURRENT_USER':
      return { ...state, currentUser: action.payload };
    case 'SET_ACCOUNTS':
      return { ...state, accounts: action.payload };
    case 'SET_CURRENT_ACCOUNT':
      return { ...state, currentAccount: action.payload };
    case 'SET_TRANSACTIONS':
      return { ...state, transactions: action.payload };
    case 'SET_TAGS':
      return { ...state, tags: action.payload };
    case 'ADD_TRANSACTION':
      return {
        ...state,
        transactions: [...state.transactions, action.payload],
        accounts: state.accounts.map((acc) => {
          if (acc.id === action.payload.accountId) {
            return {
              ...acc,
              balance:
                  acc.balance +
                  (action.payload.type === 'income'
                      ? action.payload.amount
                      : -action.payload.amount),
            };
          }
          return acc;
        }),
      };
    case 'UPDATE_TRANSACTION':
      const oldTransaction = state.transactions.find(
          (t) => t.id === action.payload.id
      );
      return {
        ...state,
        transactions: state.transactions.map((t) =>
            t.id === action.payload.id ? action.payload : t
        ),
        accounts: state.accounts.map((acc) => {
          if (acc.id === action.payload.accountId) {
            const oldEffect =
                oldTransaction.type === 'income'
                    ? -oldTransaction.amount
                    : oldTransaction.amount;
            const newEffect =
                action.payload.type === 'income'
                    ? action.payload.amount
                    : -action.payload.amount;
            return {
              ...acc,
              balance: acc.balance + oldEffect + newEffect,
            };
          }
          return acc;
        }),
      };
    case 'DELETE_TRANSACTION':
      const transactionToDelete = state.transactions.find(
          (t) => t.id === action.payload
      );
      return {
        ...state,
        transactions: state.transactions.filter((t) => t.id !== action.payload),
        accounts: state.accounts.map((acc) => {
          if (acc.id === transactionToDelete?.accountId) {
            const balanceChange =
                transactionToDelete.type === 'income'
                    ? -transactionToDelete.amount
                    : transactionToDelete.amount;
            return {
              ...acc,
              balance: acc.balance + balanceChange,
            };
          }
          return acc;
        }),
      };
    case 'ADD_ACCOUNT':
      return { ...state, accounts: [...state.accounts, action.payload] };
    case 'UPDATE_ACCOUNT':
      return {
        ...state,
        accounts: state.accounts.map((a) =>
            a.id === action.payload.id ? action.payload : a
        ),
      };
    case 'DELETE_ACCOUNT':
      return {
        ...state,
        accounts: state.accounts.filter((a) => a.id !== action.payload),
        transactions: state.transactions.filter(
            (t) => t.accountId !== action.payload
        ),
      };
    case 'ADD_TAG':
      return { ...state, tags: [...state.tags, action.payload] };
    case 'UPDATE_TAG':
      return {
        ...state,
        tags: state.tags.map((t) =>
            t.id === action.payload.id ? action.payload : t
        ),
      };
    case 'DELETE_TAG':
      return { ...state, tags: state.tags.filter((t) => t.id !== action.payload) };
    default:
      return state;
  }
};

export const useFinanceData = () => {
  const [state, dispatch] = useReducer(financeReducer, initialState);

  useEffect(() => {
    const fetchData = async () => {
      dispatch({ type: 'SET_LOADING', payload: true });
      try {
        console.log('Fetching users from API...');
        const users = await api.users.getAll();
        console.log('Received users:', users);

        if (!users || users.length === 0) {
          console.warn('No users received from API');
          dispatch({ type: 'SET_ERROR', payload: 'No users found' });
          return;
        }

        const formattedUsers = users.map((user) => ({
          id: user.id,
          name: user.name,
          email: user.email,
        }));
        dispatch({ type: 'SET_USERS', payload: formattedUsers });

        const savedUserId = localStorage.getItem('currentUserId');
        const currentUser = savedUserId
            ? formattedUsers.find((u) => u.id === parseInt(savedUserId)) ||
            formattedUsers[0]
            : formattedUsers[0];
        console.log('Selected current user:', currentUser);

        if (!currentUser) {
          console.warn('No current user selected');
          dispatch({ type: 'SET_ERROR', payload: 'No current user available' });
          return;
        }

        dispatch({ type: 'SET_CURRENT_USER', payload: currentUser });

        console.log('Fetching accounts for user:', currentUser.id);
        const accounts = await api.accounts.getByUserId(currentUser.id);
        const formattedAccounts = accounts.map((account) => ({
          id: account.id,
          name: account.name,
          balance: account.balance,
          userId: account.userId,
        }));
        console.log('Received accounts:', formattedAccounts);
        dispatch({ type: 'SET_ACCOUNTS', payload: formattedAccounts });

        dispatch({
          type: 'SET_CURRENT_ACCOUNT',
          payload: formattedAccounts[0] || null,
        });

        console.log('Fetching transactions for user:', currentUser.id);
        const transactions = await api.transactions.getByUserId(currentUser.id);
        const formattedTransactions = transactions.map((transaction) => {
          console.log('Raw transaction (initial fetch):', transaction);
          return {
            id: transaction.id,
            name: transaction.name || 'Unnamed',
            amount: transaction.amount,
            description: transaction.description || '',
            date: transaction.date,
            type: transaction.type ? 'income' : 'expense',
            userId: transaction.userId,
            accountId: transaction.billId,
            tags: transaction.tags || [],
          };
        });
        console.log('Formatted transactions (initial fetch):', formattedTransactions);
        dispatch({ type: 'SET_TRANSACTIONS', payload: formattedTransactions });

        console.log('Fetching tags for user:', currentUser.id);
        const tags = await api.tags.getByUserId(currentUser.id);
        const formattedTags = tags.map((tag) => ({
          id: tag.id,
          title: tag.name, // Предполагаем, что бэкенд возвращает name вместо title
          userId: tag.userId,
        }));
        console.log('Received tags:', formattedTags);
        dispatch({ type: 'SET_TAGS', payload: formattedTags });
      } catch (error) {
        console.error('Failed to fetch data:', error);
        dispatch({ type: 'SET_ERROR', payload: error.message });
      } finally {
        dispatch({ type: 'SET_LOADING', payload: false });
      }
    };

    fetchData();
  }, []);

  useEffect(() => {
    if (state.currentUser) {
      const fetchUserData = async () => {
        dispatch({ type: 'SET_LOADING', payload: true });
        try {
          console.log('Fetching accounts for user:', state.currentUser.id);
          const accounts = await api.accounts.getByUserId(state.currentUser.id);
          const formattedAccounts = accounts.map((account) => ({
            id: account.id,
            name: account.name,
            balance: account.balance,
            userId: account.userId,
          }));
          dispatch({ type: 'SET_ACCOUNTS', payload: formattedAccounts });
          dispatch({
            type: 'SET_CURRENT_ACCOUNT',
            payload: formattedAccounts[0] || null,
          });

          console.log('Fetching transactions for user:', state.currentUser.id);
          const transactions = await api.transactions.getByUserId(
              state.currentUser.id
          );
          const formattedTransactions = transactions.map((transaction) => {
            console.log('Raw transaction (user update):', transaction);
            return {
              id: transaction.id,
              name: transaction.name || 'Unnamed',
              amount: transaction.amount,
              description: transaction.description || '',
              date: transaction.date,
              type: transaction.type ? 'income' : 'expense',
              userId: transaction.userId,
              accountId: transaction.billId,
              tags: transaction.tags || [],
            };
          });
          console.log(
              'Formatted transactions (user update):',
              formattedTransactions
          );
          dispatch({ type: 'SET_TRANSACTIONS', payload: formattedTransactions });

          console.log('Fetching tags for user:', state.currentUser.id);
          const tags = await api.tags.getByUserId(state.currentUser.id);
          const formattedTags = tags.map((tag) => ({
            id: tag.id,
            title: tag.title,
            userId: tag.userId,
          }));
          dispatch({ type: 'SET_TAGS', payload: formattedTags });
        } catch (error) {
          console.error('Failed to fetch user data:', error);
          dispatch({ type: 'SET_ERROR', payload: error.message });
        } finally {
          dispatch({ type: 'SET_LOADING', payload: false });
        }
      };
      fetchUserData();
    }
  }, [state.currentUser]);

  return { state, dispatch };
};