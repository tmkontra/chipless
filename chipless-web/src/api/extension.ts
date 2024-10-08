import type { PlayerAction } from './gen'

export function actionTypeName(action: PlayerAction): string {
  switch (action.actionType) {
    case 'CHECK':
      return 'Check'
    case 'FOLD':
      return 'Fold'
    case 'BET':
      return 'Bet'
    case 'RAISE':
      return 'Raise'
    case 'CALL':
      return 'Call'
  }
}
