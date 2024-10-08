import type { PlayerAction } from './gen'

export function actionDisplay(action: PlayerAction): string {
  switch (action.actionType) {
    case 'CHECK':
      return 'Check'
    case 'FOLD':
      return 'Fold'
    case 'BET':
      return 'Bet ' + action.chipCount
    case 'RAISE':
      return 'Raise ' + action.chipCount
    case 'CALL':
      return 'Call ' + action.chipCount
  }
}
