import { getStyle } from 'element-plus/es/utils/dom/style';

/**
 * show-overflow-tooltip for text
 * 当text没有被折叠时，不显示tooltip
 */
export const akTooltipAutoShow = {
    created(el, binding, vnode) {
        let tooltipNode = vnode.children.find((childrenCmpt) => childrenCmpt.component?.type.name == 'ElTooltip');
        if (tooltipNode) {
            el.addEventListener('mouseenter', (e) => {
                tooltipNode.component.props.disabled = true;
                const range = document.createRange();
                range.setStart(el, 0);
                range.setEnd(el, el.childNodes.length);
                const rangeWidth = Math.round(range.getBoundingClientRect().width);
                // const padding = (parseInt(getStyle(el, 'paddingLeft'), 10) || 0) + (parseInt(getStyle(el, 'paddingRight'), 10) || 0);
                // if (rangeWidth + padding > el.offsetWidth || el.scrollWidth > el.offsetWidth) {
                if (rangeWidth > el.offsetWidth || el.scrollWidth > el.offsetWidth) {
                    tooltipNode.component.props.disabled = false;
                }
            });
        }
    }
};