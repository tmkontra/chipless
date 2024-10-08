// @ts-ignore

const Tailwind_PT = {
  button: {
    root: {
      class: [
        'text-gray',
        'hover:bg-gray-300 hover:border-gray-300 relative',
        'inline-flex items-center text-center overflow-hidden',
        'cursor-pointer px-3 py-2 text-base rounded-full'
      ]
    },
    label: {
      class: 'hidden'
    }
  },
  dialog: {
    root: {
      class:
        'relative flex flex-col max-h-90 transform scale-100 m-0 w-[80vw] z-40 rounded-lg shadow-lg border-0'
    },
    header: {
      class:
        'flex items-center justify-between flex-shrink-0 bg-white text-gray-800 border-t-0 rounded-tl-lg rounded-tr-lg p-6'
    },
    headerTitle: {
      class: 'font-bold text-lg'
    },
    headerIcons: {
      class: 'flex items-center'
    },
    closeButton: {
      class: [
        'flex items-center justify-center overflow-hidden relative',
        'w-8 h-8 text-gray-500 border-0 bg-transparent rounded-full transition duration-200 ease-in-out mr-2 last:mr-0',
        'hover:text-gray-700 hover:border-transparent hover:bg-gray-200',
        'focus:outline-none focus:outline-offset-0 focus:shadow-[0_0_0_0.2rem_rgba(191,219,254,1)]' // focus
      ]
    },
    closeButtonIcon: {
      class: 'w-4 h-4 inline-block'
    },
    content: {
      class: 'overflow-y-auto bg-white text-gray-700 px-6 pb-8 pt-0 rounded-bl-lg rounded-br-lg'
    },
    mask: {
      class:
        'fixed top-0 left-0 w-full h-full flex items-center justify-center pointer-events-auto bg-black bg-opacity-40 transition duration-200 z-20'
    }
  },
  picklist: {
    root: 'flex flex-col [&_[data-pc-name=pclist]]:h-full lg:flex-row',
    sourceControls: {
      class: ['flex lg:flex-col justify-center gap-2', 'p-5']
    },
    sourceListContainer: {
      class: [
        'grow shrink basis-2/4',
        'rounded-md',
        'bg-surface-0',
        'border border-surface-200',
        'outline-none'
      ]
    },
    transferControls: {
      class: 'flex lg:flex-col justify-center gap-2 p-5'
    },
    targetListContainer: {
      class: [
        'grow shrink basis-2/4',
        'rounded-md',
        'bg-surface-0',
        'border border-surface-200',
        'outline-none'
      ]
    },
    targetControls: {
      class: 'flex lg:flex-col justify-center gap-2 p-5'
    },
    transition: {
      enterFromClass: '!transition-none',
      enterActiveClass: '!transition-none',
      leaveActiveClass: '!transition-none',
      leaveToClass: '!transition-none'
    }
  },

  listbox: {
    root: ({ props }) => ({
      class: [
        // Sizing and Shape
        'min-w-[12rem]',
        'rounded-md',
        // Colors
        'bg-surface-0',
        'text-surface-700',
        'border',
        { 'border-surface-300': !props.invalid },
        // Invalid State
        { 'border-red-500': props.invalid }
      ]
    }),
    listContainer: 'overflow-auto',
    list: {
      class: 'py-3 list-none m-0 outline-none'
    },
    option: ({ context, props }) => ({
      class: [
        'relative',
        // Font
        'font-normal',
        'leading-none',
        // Flex
        'flex items-center',
        // Position
        'relative',
        // Shape
        'border-0',
        'rounded-none',
        // Spacing
        'm-0',
        'py-3 px-5',
        // Colors
        {
          'text-surface-700': !context.focused && !context.selected,
          'bg-surface-200': context.focused && !context.selected,
          'text-surface-700': context.focused && !context.selected,
          'bg-highlight': context.selected && !props.checkmark,
          'bg-surface-0': props.checkmark && context.selected
        },
        //States
        {
          'hover:bg-surface-100':
            (!context.focused && !context.selected) || (props.checkmark && context.selected)
        },
        { 'hover:bg-highlight-emphasis': context.selected && !props.checkmark },
        'focus-visible:outline-none focus-visible:outline-offset-0 focus-visible:ring focus-visible:ring-inset focus-visible:ring-primary-400/50',
        // Transitions
        'transition-shadow',
        'duration-200',
        // Misc
        'cursor-pointer',
        'overflow-hidden',
        'whitespace-nowrap'
      ]
    }),
    optionGroup: {
      class: ['font-bold', 'm-0', 'py-3 px-5', 'text-surface-800', 'bg-surface-0', 'cursor-auto']
    },
    optionCheckIcon: 'relative -ms-1.5 me-1.5 text-surface-700w-4 h-4',
    header: {
      class: [
        'py-3 px-5',
        'm-0',
        'border-b',
        'rounded-tl-md',
        'rounded-tr-md',
        'text-surface-700',
        'bg-surface-100',
        'border-surface-300',
        '[&_[data-pc-name=pcfilter]]:w-full'
      ]
    },
    emptyMessage: {
      class: ['leading-none', 'py-3 px-5', 'text-surface-800', 'bg-transparent']
    }
  }
}

export default Tailwind_PT
