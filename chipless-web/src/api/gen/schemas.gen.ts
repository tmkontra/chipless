// This file is auto-generated by @hey-api/openapi-ts

export const PlayerActionSchema = {
    required: ['actionType'],
    type: 'object',
    properties: {
        actionType: {
            type: 'string',
            enum: ['CHECK', 'FOLD', 'BET', 'RAISE', 'CALL']
        },
        chipCount: {
            type: 'integer',
            format: 'int32'
        }
    }
} as const;

export const BettingRoundSchema = {
    required: ['actions', 'currentPlayer', 'id', 'isClosed', 'players', 'sequence'],
    type: 'object',
    properties: {
        id: {
            type: 'string',
            format: 'uuid'
        },
        sequence: {
            type: 'integer',
            format: 'int32'
        },
        players: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/Player'
            }
        },
        currentPlayer: {
            '$ref': '#/components/schemas/Player'
        },
        actions: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/PlayerAction'
            }
        },
        isClosed: {
            type: 'boolean'
        }
    }
} as const;

export const CashoutSchema = {
    required: ['amount'],
    type: 'object',
    properties: {
        amount: {
            type: 'integer',
            format: 'int32'
        }
    }
} as const;

export const HandSchema = {
    required: ['currentRound', 'id', 'isFinished', 'players', 'rounds', 'sequence'],
    type: 'object',
    properties: {
        id: {
            type: 'string',
            format: 'uuid'
        },
        sequence: {
            type: 'integer',
            format: 'int32'
        },
        players: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/HandPlayer'
            }
        },
        rounds: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/BettingRound'
            }
        },
        currentRound: {
            '$ref': '#/components/schemas/BettingRound'
        },
        isFinished: {
            type: 'boolean'
        }
    }
} as const;

export const HandPlayerSchema = {
    required: ['initialChips', 'isDealer', 'player'],
    type: 'object',
    properties: {
        player: {
            '$ref': '#/components/schemas/Player'
        },
        winnings: {
            type: 'integer',
            format: 'int32'
        },
        wager: {
            type: 'integer',
            format: 'int32'
        },
        initialChips: {
            type: 'integer',
            format: 'int32'
        },
        isDealer: {
            type: 'boolean'
        },
        lastAction: {
            '$ref': '#/components/schemas/PlayerAction'
        }
    }
} as const;

export const PlayerSchema = {
    required: ['buyCount', 'id', 'name'],
    type: 'object',
    properties: {
        id: {
            type: 'integer',
            format: 'int64'
        },
        name: {
            type: 'string'
        },
        buyCount: {
            type: 'integer',
            format: 'int32'
        }
    }
} as const;

export const PlayerAdminViewSchema = {
    required: ['cashouts', 'outstandingChips', 'player', 'shortCode'],
    type: 'object',
    properties: {
        player: {
            '$ref': '#/components/schemas/Player'
        },
        shortCode: {
            type: 'string'
        },
        cashouts: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/Cashout'
            }
        },
        outstandingChips: {
            type: 'integer',
            format: 'int32'
        }
    }
} as const;

export const PlayerHandViewSchema = {
    required: ['availableActions', 'availableChips', 'hand', 'isTurn', 'player'],
    type: 'object',
    properties: {
        hand: {
            '$ref': '#/components/schemas/Hand'
        },
        player: {
            '$ref': '#/components/schemas/PlayerAdminView'
        },
        isTurn: {
            type: 'boolean'
        },
        availableActions: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/PlayerAction'
            }
        },
        availableChips: {
            type: 'integer',
            format: 'int32'
        }
    }
} as const;

export const CreateGameSchema = {
    required: ['buyinAmount', 'buyinChips', 'name'],
    type: 'object',
    properties: {
        name: {
            type: 'string'
        },
        buyinAmount: {
            type: 'number'
        },
        buyinChips: {
            type: 'integer',
            format: 'int32'
        }
    }
} as const;

export const GameSchema = {
    required: ['buyinAmount', 'buyinChips', 'id', 'name', 'players', 'shortCode'],
    type: 'object',
    properties: {
        id: {
            type: 'integer',
            format: 'int64'
        },
        name: {
            type: 'string'
        },
        shortCode: {
            type: 'string'
        },
        buyinAmount: {
            type: 'number'
        },
        buyinChips: {
            type: 'integer',
            format: 'int32'
        },
        players: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/Player'
            }
        }
    }
} as const;

export const GameAdminViewSchema = {
    required: ['adminCode', 'game', 'hands', 'newPlayers', 'nextHandOrder', 'players', 'playersBankrupt'],
    type: 'object',
    properties: {
        game: {
            '$ref': '#/components/schemas/Game'
        },
        adminCode: {
            type: 'string'
        },
        players: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/PlayerAdminView'
            }
        },
        hands: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/Hand'
            }
        },
        currentHand: {
            '$ref': '#/components/schemas/Hand'
        },
        nextHandOrder: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/PlayerAdminView'
            }
        },
        playersBankrupt: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/PlayerAdminView'
            }
        },
        newPlayers: {
            type: 'array',
            items: {
                '$ref': '#/components/schemas/PlayerAdminView'
            }
        }
    }
} as const;